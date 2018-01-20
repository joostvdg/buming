# buming 不明
Attempting to build a Concurrent, Modular, Domain Driven, Java 9 based Network application utilizing Streams &amp; Lambda's app. But its not clear if that is what it is going to be.

## TODO

* Gossip protocol for sending data

### Membership protocol

* Membership protocol should allow a process to leave
    * requires graceful shutdown!
* How do we start the membership lists, as we don't know each other: Broadcast or Multicast
    * [Baeldun UDP](http://www.baeldung.com/udp-in-java)
    * [Baeldung Multicast](http://www.baeldung.com/java-broadcast-multicast)
    * Broadcast for the initial discovery phase --> or can Multicast with a specific group work fine as well?
        * Means we need to reply to this and return our name (and counter?)
    * Multicast for the subsequent membership message
* Add internal message counter
* Add "system" message counter --> for the consensus part
