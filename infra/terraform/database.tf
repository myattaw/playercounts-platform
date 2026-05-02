resource "aws_db_subnet_group" "playercounts_db_subnet_group" {
  name = "${var.project_name}-${var.environment}-db-subnet-group"

  subnet_ids = [
    aws_subnet.public_subnet_a.id,
    aws_subnet.public_subnet_b.id
  ]
}
resource "aws_db_instance" "playercounts_postgres" {
  identifier             = "${var.project_name}-${var.environment}-postgres"
  engine                 = "postgres"
  engine_version         = "16.3"
  instance_class         = "db.t4g.micro"
  allocated_storage      = 20
  db_name                = "playercounts"
  username               = "playercounts"
  password               = "playercounts123!"
  publicly_accessible    = true
  skip_final_snapshot    = true
  db_subnet_group_name   = aws_db_subnet_group.playercounts_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.internal_services_sg.id]
}

resource "aws_elasticache_subnet_group" "playercounts_cache_subnet_group" {
  name = "${var.project_name}-${var.environment}-cache-subnet-group"

  subnet_ids = [
    aws_subnet.public_subnet_a.id,
    aws_subnet.public_subnet_b.id
  ]
}

resource "aws_elasticache_cluster" "playercounts_redis" {
  cluster_id           = "${var.project_name}-${var.environment}-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.playercounts_cache_subnet_group.name
  security_group_ids   = [aws_security_group.internal_services_sg.id]
}