package com.reviq.shared.seeder;

public interface Seeder {

    void seed();

    int getOrder();

    String getName();
}
