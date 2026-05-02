output "poll_worker_ecr_url" {
  value = aws_ecr_repository.poll_worker_repo.repository_url
}

output "status_consumer_ecr_url" {
  value = aws_ecr_repository.status_consumer_repo.repository_url
}

output "api_gateway_ecr_url" {
  value = aws_ecr_repository.api_gateway_repo.repository_url
}

output "sqs_queue_url" {
  value = aws_sqs_queue.telemetry_events_queue.id
}

output "postgres_endpoint" {
  value = aws_db_instance.playercounts_postgres.address
}

output "redis_endpoint" {
  value = aws_elasticache_cluster.playercounts_redis.cache_nodes[0].address
}

output "alb_dns_name" {
  value = aws_lb.playercounts_alb.dns_name
}