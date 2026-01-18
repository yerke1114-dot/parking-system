package com.company;

import com.company.controllers.interfaces.AuthorizationInterface;
import com.company.data.PostgresDB;
import com.company.data.interfaces.IDB;
import com.company.repositories.AuthorizationRepository;
import com.company.repositories.ParkingRepository;
import com.company.repositories.interfaces.IParkingRepository;
import com.company.repositories.AdminRepository;
import com.company.repositories.interfaces.IAdminRepository;

public class Main {
    public static void main(String[] args) {

        IDB db = new PostgresDB("jdbc:postgresql://localhost:5432", "postgres", "0000", "Db1");

        AuthorizationInterface auth = new AuthorizationRepository(db);
        IParkingRepository parkingRepo = new ParkingRepository(db);
        IAdminRepository adminRepo = new AdminRep(db);

        MyApplication app = new MyApplication(auth, parkingRepo, adminRepo);
        app.start();

        db.close();
    }
}
