package com.company;

import com.company.controllers.AdminController;
import com.company.controllers.ParkingController;
import com.company.repositories.interfaces.AuthorizationInterface;
import com.company.data.PostgresDB;
import com.company.data.interfaces.IDB;
import com.company.repositories.AdminRepository;
import com.company.repositories.AuthorizationRepository;
import com.company.repositories.ParkingRepository;
import com.company.repositories.interfaces.IParkingRepository;

public class Main {
    public static void main(String[] args) {

        IDB db = PostgresDB.getInstance("jdbc:postgresql://localhost:5432", "postgres", "0000", "Db1");

        AdminRepository adminRepo = new AdminRepository(db);
        AdminController adminController = new AdminController(adminRepo);
        IParkingRepository parkingRepo = new ParkingRepository(db);
        ParkingController parkingController = new ParkingController(parkingRepo);
        AuthorizationInterface auth = new AuthorizationRepository(db);

        MyApplication app = new MyApplication(auth, parkingController, adminController);

        app.start();

        app.start();

        db.close();
    }
}