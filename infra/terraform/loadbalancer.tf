resource "aws_lb" "playercounts_alb" {
  name               = "${var.project_name}-${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.api_sg.id]
  subnets = [
    aws_subnet.public_subnet_a.id,
    aws_subnet.public_subnet_b.id
  ]
}

resource "aws_lb_target_group" "api_gateway_tg" {
  name        = "${var.project_name}-${var.environment}-api-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.playercounts_vpc.id
  target_type = "ip"

  health_check {
    path = "/servers/live"
    port = "8080"
  }
}

resource "aws_lb_listener" "http_listener" {
  load_balancer_arn = aws_lb.playercounts_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway_tg.arn
  }
}