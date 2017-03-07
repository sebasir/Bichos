package net.hpclab.cev.beans;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import net.hpclab.cev.entities.Catalog;
import net.hpclab.cev.entities.Collection;
import net.hpclab.cev.services.DataBaseService;

@ManagedBean
@SessionScoped
public class CatalogBean extends UtilsBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private Catalog catalog;
    private List<Catalog> allCatalogs;
    private String selectedCont;

    public CatalogBean() {
    }

    @PostConstruct
    public void init() {
    }

    public String persist() {
        try {
            catalog.setIdCollection(new Collection(new Integer(getSelectedCont())));
            DataBaseService<Catalog> dbm = new DataBaseService<>(Catalog.class);
            setCatalog(dbm.persist(catalog));
            if (getCatalog() != null && getCatalog().getIdCatalog() != null) {
                FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.CREATE_SUCCESS));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.CREATE_ERROR));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.CREATE_ERROR));
        }

        return findAllCatalogs();
    }

    public void delete() {
        try {
            //catalogSession.delete(getCatalog());
            FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.DELETE_SUCCESS));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.DELETE_ERROR));
        }
    }

    public void prepareCreate() {
        setCatalog(new Catalog());
    }

    public void edit() {
        try {
            //setCatalog(catalogSession.merge(getCatalog()));
            FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.UPDATE_SUCCESS));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(catalog, Operations.UPDATE_ERROR));
        }
    }

    public String displayList() {
        findAllCatalogs();
        return "specimen";
    }

    public String findAllCatalogs() {
        //setAllCatalogs(catalogSession.listAll());
        return null;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public List<Catalog> getAllCatalogs() {
        return allCatalogs;
    }

    public void setAllCatalogs(List<Catalog> allCatalogs) {
        this.allCatalogs = allCatalogs;
    }

    public String getSelectedCont() {
        return selectedCont;
    }

    public void setSelectedCont(String selectedCont) {
        this.selectedCont = selectedCont;
    }
}