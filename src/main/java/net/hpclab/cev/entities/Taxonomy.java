/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hpclab.cev.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Sebasir
 */
@Entity
@Table(name = "taxonomy")
@NamedQueries({
    @NamedQuery(name = "Taxonomy.findAll", query = "SELECT t FROM Taxonomy t")})
public class Taxonomy implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_taxonomy")
    private Integer idTaxonomy;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "taxonomy_name")
    private String taxonomyName;
    @Size(max = 2147483647)
    @Column(name = "status")
    private String status;
    @OneToMany(mappedBy = "idContainer")
    private List<Taxonomy> taxonomyList;
    @JoinColumn(name = "id_container", referencedColumnName = "id_taxonomy")
    @ManyToOne
    private Taxonomy idContainer;
    @JoinColumn(name = "id_taxlevel", referencedColumnName = "id_taxlevel")
    @ManyToOne
    private TaxonomyLevel idTaxlevel;
    @OneToOne(mappedBy = "idTaxonomy")
    private Specimen specimen;

    public Taxonomy() {
    }

    public Taxonomy(Integer idTaxonomy) {
        this.idTaxonomy = idTaxonomy;
    }

    public Taxonomy(Integer idTaxonomy, String taxonomyName) {
        this.idTaxonomy = idTaxonomy;
        this.taxonomyName = taxonomyName;
    }

    public Integer getIdTaxonomy() {
        return idTaxonomy;
    }

    public void setIdTaxonomy(Integer idTaxonomy) {
        this.idTaxonomy = idTaxonomy;
    }

    public String getTaxonomyName() {
        return taxonomyName;
    }

    public void setTaxonomyName(String taxonomyName) {
        this.taxonomyName = taxonomyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Taxonomy> getTaxonomyList() {
        return taxonomyList;
    }

    public void setTaxonomyList(List<Taxonomy> taxonomyList) {
        this.taxonomyList = taxonomyList;
    }

    public Taxonomy getIdContainer() {
        return idContainer;
    }

    public void setIdContainer(Taxonomy idContainer) {
        this.idContainer = idContainer;
    }

    public TaxonomyLevel getIdTaxlevel() {
        return idTaxlevel;
    }

    public void setIdTaxlevel(TaxonomyLevel idTaxlevel) {
        this.idTaxlevel = idTaxlevel;
    }

    public Specimen getSpecimen() {
        return specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTaxonomy != null ? idTaxonomy.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Taxonomy)) {
            return false;
        }
        Taxonomy other = (Taxonomy) object;
        if ((this.idTaxonomy == null && other.idTaxonomy != null) || (this.idTaxonomy != null && !this.idTaxonomy.equals(other.idTaxonomy))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.hpclab.entities.Taxonomy[ idTaxonomy=" + idTaxonomy + " ]";
    }
    
}