package com.creditville.notifications.services;

import com.creditville.notifications.models.CollectionOfficer;

import java.util.List;

public interface CollectionOfficerService {
    CollectionOfficer getCollectionOfficer(String branch);

    CollectionOfficer createNew(String branch, String officerName, String officerEmail, String officerPhoneNo);

    List<CollectionOfficer> getAllCollectionOfficers();
}
