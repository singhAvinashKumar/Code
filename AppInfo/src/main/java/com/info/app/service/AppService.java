package com.info.app.service;

import com.info.app.model.AppInfo;
import com.info.app.model.AppRating;
import com.info.app.repo.AppRatingRepo;
import com.info.app.repo.AppRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Component
public class AppService {

    @Autowired
    private AppRepo repo;
    @Autowired
    private AppRatingRepo ratingRepo;

    @CircuitBreaker(name="Catalog",fallbackMethod = "getDefaultApps")
    public Iterable<AppInfo> getAllApps() {
        return  repo.findAll();
    }

    public Iterable<AppInfo> getDefaultApps(){
        List<AppInfo> list= new ArrayList<>();
        list.add(new AppInfo(0,"None","None"));
        return list;
    }

    public Optional<AppInfo> getApp(int appId) {
        return repo.findById(appId);

    }

    public String uploadApp(AppInfo a) {
        repo.save(a);
        return "App uploaded";
    }

    public String deleteApp(int appId){
        Optional<AppInfo> a = repo.findById(appId);
        if(!a.isEmpty()){
            repo.deleteById(appId);
            return "App Deleted";
        }


        return "User Don't exists";
    }

    public String rateApp(AppRating ratings){
        ratingRepo.save(ratings);
        return "Ratings Updated";
    }

    public Iterable<AppRating> getRatings() {
        return ratingRepo.findAll();
    }
}
