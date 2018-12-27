package es.upm.hcid.newsmanager.models;

import es.upm.hcid.newsmanager.assignment.ModelManager;

public class ServiceManager {

    private static volatile ServiceManager instance;

    private ModelManager mm;

    private ServiceManager(){
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) instance = new ServiceManager();
            }
        }
        return instance;
    }

    public void setModelManager(ModelManager mm){
        this.mm = mm;
    }

    public ModelManager getModelManager(){
        return this.mm;
    }
}
