resource "aws_ecr_repository" "poll_worker_repo" {
  name = "${var.project_name}-poll-worker"
}

resource "aws_ecr_repository" "status_consumer_repo" {
  name = "${var.project_name}-status-consumer"
}

resource "aws_ecr_repository" "api_gateway_repo" {
  name = "${var.project_name}-api-gateway"
}

resource "aws_ecs_cluster" "playercounts_cluster" {
  name = "${var.project_name}-${var.environment}-cluster"
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.project_name}-${var.environment}-ecs-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
      Effect = "Allow"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Poll worker task
resource "aws_ecs_task_definition" "poll_worker_task" {
  family                   = "${var.project_name}-poll-worker"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "poll-worker"
      image     = "${aws_ecr_repository.poll_worker_repo.repository_url}:latest"
      essential = true

      environment = [
        { name = "SQS_QUEUE_URL", value = aws_sqs_queue.telemetry_events_queue.id }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/playercounts-poll-worker"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}

# Status consumer task
resource "aws_ecs_task_definition" "status_consumer_task" {
  family                   = "${var.project_name}-status-consumer"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "status-consumer"
      image     = "${aws_ecr_repository.status_consumer_repo.repository_url}:latest"
      essential = true

      environment = [
        { name = "REDIS_HOST", value = aws_elasticache_cluster.playercounts_redis.cache_nodes[0].address },
        { name = "POSTGRES_JDBC_URL", value = "jdbc:postgresql://${aws_db_instance.playercounts_postgres.address}:5432/playercounts" },
        { name = "POSTGRES_USERNAME", value = "playercounts" },
        { name = "POSTGRES_PASSWORD", value = "playercounts123!" },
        { name = "SQS_QUEUE_URL", value = aws_sqs_queue.telemetry_events_queue.id }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/playercounts-status-consumer"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}

# API gateway task
resource "aws_ecs_task_definition" "api_gateway_task" {
  family                   = "${var.project_name}-api-gateway"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "api-gateway"
      image     = "${aws_ecr_repository.api_gateway_repo.repository_url}:latest"
      essential = true

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]

      environment = [
        { name = "REDIS_HOST", value = aws_elasticache_cluster.playercounts_redis.cache_nodes[0].address },
        { name = "POSTGRES_JDBC_URL", value = "jdbc:postgresql://${aws_db_instance.playercounts_postgres.address}:5432/playercounts" },
        { name = "POSTGRES_USERNAME", value = "playercounts" },
        { name = "POSTGRES_PASSWORD", value = "playercounts123!" }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/playercounts-api-gateway"
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}

# Poll Worker service

resource "aws_ecs_service" "poll_worker_service" {
  name            = "${var.project_name}-poll-worker-service"
  cluster         = aws_ecs_cluster.playercounts_cluster.id
  task_definition = aws_ecs_task_definition.poll_worker_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]
    security_groups  = [aws_security_group.internal_services_sg.id]
    assign_public_ip = true
  }
}

# Status Consumer service

resource "aws_ecs_service" "status_consumer_service" {
  name            = "${var.project_name}-status-consumer-service"
  cluster         = aws_ecs_cluster.playercounts_cluster.id
  task_definition = aws_ecs_task_definition.status_consumer_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]
    security_groups  = [aws_security_group.internal_services_sg.id]
    assign_public_ip = true
  }
}

# API Gateway service
resource "aws_ecs_service" "api_gateway_service" {
  name            = "${var.project_name}-api-gateway-service"
  cluster         = aws_ecs_cluster.playercounts_cluster.id
  task_definition = aws_ecs_task_definition.api_gateway_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]
    security_groups  = [aws_security_group.api_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api_gateway_tg.arn
    container_name   = "api-gateway"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.http_listener]
}