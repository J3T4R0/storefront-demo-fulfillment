# Kafka Traveler Microservices Demo: Orders

## Orders Service

Spring Boot/Kafka/Mongo Microservice, one of a set of microservices for this project. Services use Spring Kafka 2.1.6 to maintain eventually consistent data between their different `Customer` domain objects.

Originally code based on the post, [Spring Kafka - JSON Serializer Deserializer Example](https://www.codenotfound.com/spring-kafka-json-serializer-deserializer-example.html), from the [CodeNotFound.com](https://www.codenotfound.com/) Blog. Original business domain idea based on the post, [Distributed Sagas for Microservices](https://dzone.com/articles/distributed-sagas-for-microservices), on [DZone](https://dzone.com/).

## Development

For [Kakfa](https://kafka.apache.org/), I use my [garystafford/kafka-docker](https://github.com/garystafford/kafka-docker) project, a clone of the [wurstmeister/kafka-docker](https://github.com/wurstmeister/kafka-docker) project. The `garystafford/kafka-docker` [local docker-compose file](https://github.com/garystafford/kafka-docker/blob/master/docker-compose-local.yml) builds a Kafka, ZooKeeper, MongoDB, and Alpine Linux OpenJDK container.

## Commands

I debug directly from JetBrains IntelliJ. For testing the application in development, I build the jar, copy it to Alpine Linux OpenJDK `testapp` container, and run it. If testing more than one service in the same testapp container, make sure ports don't collide. Start services on different ports.

```bash
# build
./gradlew clean build

# copy
docker cp build/libs/orders-1.0.0.jar kafka-docker_testapp_1:/orders-1.0.0.jar
docker exec -it kafka-docker_testapp_1 sh

# install curl
apk update && apk add curl

# start with 'dev' profile
# same testapp container as accounts,
# so start on different port
java -jar orders-1.0.0.jar \
    --spring.profiles.active=dev \
    --server.port=8890
```

## Creating Sample Data

Create sample customers with an order history.
```bash
# create sample accounts customers
curl http://localhost:8080/customers/sample

# create sample orders products
curl http://localhost:8890/products/sample

# add sample order history to orders customers
# (received from kafka `accounts.customer.save` topic)
curl http://localhost:8890/customers/sample

```

## Container Infrastructure

```text
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS              PORTS                                                NAMES
f11b56e51d57        openjdk:8u151-jdk-alpine3.7     "sleep 6000"             4 seconds ago       Up 3 seconds                                                             kafka-docker_testapp_1
ede27d4c993b        mongo:latest                    "docker-entrypoint.s…"   21 hours ago        Up 21 hours         0.0.0.0:27017->27017/tcp                             kafka-docker_mongo_1
fde71dcb89be        wurstmeister/kafka:latest       "start-kafka.sh"         21 hours ago        Up 21 hours         0.0.0.0:9092->9092/tcp                               kafka-docker_kafka_1
538397f51320        wurstmeister/zookeeper:latest   "/bin/sh -c '/usr/sb…"   21 hours ago        Up 21 hours         22/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp   kafka-docker_zookeeper_1
```
## Orders Customer Object in MongoDB

`db.customer.find().pretty();`

```bson
{
	"_id" : ObjectId("5b0c54e2be41760051d00383"),
	"name" : {
		"title" : "Mr.",
		"firstName" : "John",
		"middleName" : "S.",
		"lastName" : "Doe",
		"suffix" : "Jr."
	},
	"contact" : {
		"primaryPhone" : "555-666-7777",
		"secondaryPhone" : "555-444-9898",
		"email" : "john.doe@internet.com"
	},
	"orders" : [
		{
			"timestamp" : NumberLong("1527538871249"),
			"status" : "COMPLETED",
			"orderItems" : [
				{
					"productGuid" : "b5efd4a0-4eb9-4ad0-bc9e-2f5542cbe897",
					"quantity" : 2,
					"unitPrice" : "1.99"
				},
				{
					"productGuid" : "a9d5a5c7-4245-4b4e-b1c3-1d3968f36b2d",
					"quantity" : 4,
					"unitPrice" : "5.99"
				},
				{
					"productGuid" : "f3b9bdce-10d8-4c22-9861-27149879b3c1",
					"quantity" : 1,
					"unitPrice" : "9.99"
				},
				{
					"productGuid" : "b506b962-fcfa-4ad6-a955-8859797edf16",
					"quantity" : 3,
					"unitPrice" : "13.99"
				}
			]
		},
		{
			"timestamp" : NumberLong("1527538871249"),
			"status" : "PROCESSING",
			"orderItems" : [
				{
					"productGuid" : "d01fde07-7c24-49c5-a5f1-bc2ce1f14c48",
					"quantity" : 5,
					"unitPrice" : "3.99"
				},
				{
					"productGuid" : "4efe33a1-722d-48c8-af8e-7879edcad2fa",
					"quantity" : 2,
					"unitPrice" : "7.99"
				},
				{
					"productGuid" : "7f3c9c22-3c0a-47a5-9a92-2bd2e23f6e37",
					"quantity" : 4,
					"unitPrice" : "11.99"
				}
			]
		}
	],
	"_class" : "com.storefront.model.Customer"
}
```

## Current Results

Output from application, on the `accounts.customer.save` topic

```text
2018-05-28 19:11:19.383  INFO 22 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-1, groupId=json] Setting newly assigned partitions [accounts-0]

2018-05-28 19:11:19.388  INFO 22 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : partitions assigned: [accounts-0]

2018-05-28 19:13:39.565  INFO 22 --- [ntainer#0-0-C-1] com.storefront.kafka.Receiver            : received payload='Customer(id=5b0c54e2be41760051d00383, accountsId=null, name=Name(title=Mr., firstName=John, middleName=S., lastName=Doe, suffix=Jr.), contact=Contact(primaryPhone=555-666-7777, secondaryPhone=555-444-9898, email=john.doe@internet.com), orders=null)'

2018-05-28 19:13:39.725  INFO 22 --- [ntainer#0-0-C-1] org.mongodb.driver.connection            : Opened connection [connectionId{localValue:2, serverValue:41}] to mongo:27017

2018-05-28 19:13:39.836  INFO 22 --- [ntainer#0-0-C-1] com.storefront.kafka.Receiver            : received payload='Customer(id=5b0c54e3be41760051d00384, accountsId=null, name=Name(title=Ms., firstName=Mary, middleName=null, lastName=Smith, suffix=null), contact=Contact(primaryPhone=456-789-0001, secondaryPhone=456-222-1111, email=marysmith@yougotmail.com), orders=null)'
```

Output from Kafka container using the following command.

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --from-beginning --topic accounts.customer.save
```

Kafka Consumer Output

```text
{"id":"5b0c54e2be41760051d00383","name":{"title":"Mr.","firstName":"John","middleName":"S.","lastName":"Doe","suffix":"Jr."},"contact":{"primaryPhone":"555-666-7777","secondaryPhone":"555-444-9898","email":"john.doe@internet.com"},"addresses":[{"type":"BILLING","description":"My cc billing address","address1":"123 Oak Street","address2":null,"city":"Sunrise","state":"CA","postalCode":"12345-6789"},{"type":"SHIPPING","description":"My home address","address1":"123 Oak Street","address2":null,"city":"Sunrise","state":"CA","postalCode":"12345-6789"}],"creditCards":[{"type":"PRIMARY","description":"VISA","number":"1234-6789-0000-0000","expiration":"6/19","nameOnCard":"John S. Doe"},{"type":"ALTERNATE","description":"Corporate American Express","number":"9999-8888-7777-6666","expiration":"3/20","nameOnCard":"John Doe"}],"credentials":{"username":"johndoe37","password":"skd837#$hfh485&"}}

{"id":"5b0c54e3be41760051d00384","name":{"title":"Ms.","firstName":"Mary","middleName":null,"lastName":"Smith","suffix":null},"contact":{"primaryPhone":"456-789-0001","secondaryPhone":"456-222-1111","email":"marysmith@yougotmail.com"},"addresses":[{"type":"BILLING","description":"My CC billing address","address1":"1234 Main Street","address2":null,"city":"Anywhere","state":"NY","postalCode":"45455-66677"},{"type":"SHIPPING","description":"Home Sweet Home","address1":"1234 Main Street","address2":null,"city":"Anywhere","state":"NY","postalCode":"45455-66677"}],"creditCards":[{"type":"PRIMARY","description":"VISA","number":"4545-6767-8989-0000","expiration":"7/21","nameOnCard":"Mary Smith"}],"credentials":{"username":"msmith445","password":"S*$475hf&*dddFFG3"}}
```

The `orders.order.save` topic is not used for this demo

```bash
kafka-topics.sh --create \
  --zookeeper zookeeper:2181 \
  --replication-factor 1 --partitions 1 \
  --topic orders.order.save
```

## References

-   [Spring Kafka – Consumer and Producer Example](https://memorynotfound.com/spring-kafka-consume-producer-example/)
-   [Spring Kafka - JSON Serializer Deserializer Example
    ](https://www.codenotfound.com/spring-kafka-json-serializer-deserializer-example.html)
-   [Spring for Apache Kafka: 2.1.6.RELEASE](https://docs.spring.io/spring-kafka/reference/html/index.html)
