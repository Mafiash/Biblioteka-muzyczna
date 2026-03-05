/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.lab2.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {
    
    private static final String PERSISTENCE_UNIT_NAME = "my_persistence_unit";
    
    private static final EntityManagerFactory EMF = build();

    private JpaUtil() {}

    private static EntityManagerFactory build() {
        try {
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEmf() {
        return EMF;
    }

    public static void close() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}