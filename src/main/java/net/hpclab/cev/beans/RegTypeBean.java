package net.hpclab.cev.beans;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import net.hpclab.cev.entities.RegType;

@ManagedBean
@SessionScoped
public class RegTypeBean extends UtilsBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private RegType regType;
    private List<RegType> allRegTypes;

    public RegTypeBean() {
    }

    @PostConstruct
    public void init() {
    }

    public String persist() {
        try {
            //setRegType(regTypeSession.persist(getRegType()));
            if (getRegType() != null && getRegType().getIdRety() != null) {
                FacesContext.getCurrentInstance().addMessage(null, showMessage(regType, Operations.CREATE_SUCCESS));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, showMessage(regType, Operations.CREATE_ERROR));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(regType, Operations.CREATE_ERROR));
        }

        return findAllRegTypes();
    }

    public void delete() {
        try {
            //regTypeSession.delete(getRegType());
            FacesContext.getCurrentInstance().addMessage(null, showMessage(getRegType(), Operations.DELETE_SUCCESS));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(getRegType(), Operations.DELETE_ERROR));
        }
    }

    public void prepareCreate() {
        setRegType(new RegType());
    }

    public void edit() {
        try {
            //setRegType(regTypeSession.merge(getRegType()));
            FacesContext.getCurrentInstance().addMessage(null, showMessage(getRegType(), Operations.UPDATE_SUCCESS));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, showMessage(getRegType(), Operations.UPDATE_ERROR));
        }
    }

    public String displayList() {
        findAllRegTypes();
        return "specimen";
    }

    public String findAllRegTypes() {
        //setAllRegTypes(regTypeSession.listAll());
        return null;
    }

    public RegType getRegType() {
        return regType;
    }

    public void setRegType(RegType regType) {
        this.regType = regType;
    }

    public List<RegType> getAllRegTypes() {
        return allRegTypes;
    }

    public void setAllRegTypes(List<RegType> allRegTypes) {
        this.allRegTypes = allRegTypes;
    }
}