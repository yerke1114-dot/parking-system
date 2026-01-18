package com.company;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.data.PostgresDB;
import com.company.data.interfaces.IDB;
import com.company.repositories.AuthorizationRepository;
import com.company.repositories.ParkingRepository;
import com.company.repositories.interfaces.IParkingRepository;

public class Main {
    public static void main(String[] args) {

        IDB db = new PostgresDB("jdbc:postgresql://localhost:5432", "postgres", "0000", "Db1");

        AuthorizationInterface auth = new AuthorizationRepository(db);
        IParkingRepository parkingRepo = new ParkingRepository(db);

        MyApplication app = new MyApplication(auth, parkingRepo);
        app.start();

        db.close();
    }
}