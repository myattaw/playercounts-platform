resource "aws_sqs_queue" "telemetry_events_queue" {
  name                       = "${var.project_name}-${var.environment}-telemetry-events"
  visibility_timeout_seconds = 30
  message_retention_seconds  = 345600

  tags = {
    Name = "${var.project_name}-${var.environment}-telemetry-events"
  }
}