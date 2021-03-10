package com.creditville.notifications.services.impl;

import com.creditville.notifications.models.CollectionOfficer;
import com.creditville.notifications.repositories.CollectionOfficerRepository;
import com.creditville.notifications.services.CollectionOfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionOfficerServiceImpl implements CollectionOfficerService {
    @Autowired
    private CollectionOfficerRepository collectionOfficerRepository;

    @Override
    public CollectionOfficer getCollectionOfficer(String branch) {
        return collectionOfficerRepository.findByBranch(branch);
    }

    @Override
    public CollectionOfficer createNew(String branch, String officerName, String officerEmail, String officerPhoneNo) {
        return collectionOfficerRepository.save(new CollectionOfficer(branch, officerName, officerEmail, officerPhoneNo));
    }

    @Override
    public List<CollectionOfficer> getAllCollectionOfficers() {
        return collectionOfficerRepository.findAll();
    }
}
