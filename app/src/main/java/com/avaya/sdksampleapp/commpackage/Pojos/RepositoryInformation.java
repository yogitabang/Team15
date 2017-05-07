package com.avaya.sdksampleapp.commpackage.Pojos;

import org.eclipse.egit.github.core.Repository;

import java.io.Serializable;

/**
 * Created by yogita on 7/5/17.
 */

public class RepositoryInformation implements Serializable {
    String repo_name;
    String description;
    String homepage;
    String language;
    long ID;
    Repository repository;
    public RepositoryInformation(String repnmae,String decs, String hpage, String lang, long id){
        repo_name=repnmae;
        description=decs;
        homepage=hpage;
        language=lang;
        ID=id;
    }
    public RepositoryInformation(Repository repo){
        repository = repo;
    }

    public String getRepo_name() {
        return repo_name;
    }

    public Repository getRepoObject() {
        return repository;
    }

    public String getDescription() {
        return description;
    }

    public long getID() {
        return ID;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getLanguage() {
        return language;
    }
}
