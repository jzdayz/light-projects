package io.github.jzdayz.exmaple;

import io.github.jzdayz.client.Client;

public class ClientTests {

    public static void main(String[] args) {
        Client client = new Client("localhost", 10999);
        People rpc = client
                .rpc("jzdayz", "getSomething", People.class, People.builder().name("小白").age(10).build());
        client.close();

        System.out.println(rpc);
    }
}
