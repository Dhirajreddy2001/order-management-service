#!/bin/bash
set -e

awslocal sqs create-queue \
  --queue-name invoice-jobs \
  --attributes VisibilityTimeout=30,MessageRetentionPeriod=86400

awslocal sqs create-queue \
  --queue-name invoice-jobs-dlq

echo "SQS queues created successfully."
