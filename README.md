warsjawa-ms-play
================

Microservice in Play Framework used during Warsjawa 2014 workshop "Microservices in Scala".

Running
-----

First of all, make sure you have Redis running and you stored your auth token there. For example:

```
$ sudo redis-server
$ redis-cli
127.0.0.1:6379> SET "auth:token:abc" "myLogin"
OK
```

then

```
$ sbt
[warsjawa-ms-play]$ ~run
```

After that use `test/index.html` for testing. Your auth token will be `abc`, your login `myLogin`.

Additional resources
--------------------

* [Microservices overview presentation](http://www.slideshare.net/luksow/microservices-workshopiteratorswarsjawa2014)

* [Microservices in Play & Akka presentation](http://www.slideshare.net/luksow/microservices-playworkshopiteratorswarsjawa2014)

* [Microservices in Spray presentation](http://www.slideshare.net/luksow/microservices-sprayworkshopiteratorswarsjawa2014)

* [Microservice in Spray](https://github.com/TheIterators/warsjawa-ms-spray)
