# DHT (Distributed Hash Table) library

## Description

This project is a Java implementation of a Distributed Hash Table, encapsulating its three main implementations - Chord, Pastry and Kademlia. It uses gRPC for communication between nodes and Maven for dependency management.

A distributed hash table (DHT) is a distributed system that provides a lookup service similar to a hash table. Key–value pairs are stored in a DHT, and any participating node can efficiently retrieve the value associated with a given key. The main advantage of a DHT is that nodes can be added or removed with minimum work around re-distributing keys.

## Prerequisites

- Java 17
- Maven

## Installation

1. Clone the repository: `git clone https://github.com/vkuzdas/DHT`
2. Navigate to the project directory: `cd DHT`
3. Install the dependencies: `mvn install`
