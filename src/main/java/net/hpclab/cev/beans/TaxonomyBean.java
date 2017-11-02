package net.hpclab.cev.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import net.hpclab.cev.entities.Specimen;
import net.hpclab.cev.entities.Taxonomy;
import net.hpclab.cev.entities.TaxonomyLevel;
import net.hpclab.cev.enums.OutcomeEnum;
import net.hpclab.cev.model.TreeHierachyModel;
import net.hpclab.cev.services.Constant;
import net.hpclab.cev.services.DataBaseService;

@ManagedBean
@ViewScoped
public class TaxonomyBean extends UtilsBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private DataBaseService<Specimen> specimenService;
	private DataBaseService<Taxonomy> taxonomyService;
	private DataBaseService<TaxonomyLevel> taxonomyLevelService;
	private String selectedLevel;
	private Taxonomy taxonomy;
	private Taxonomy parentTaxonomy;
	private TreeNode root;
	private TreeNode selectedNode;
	private HashMap<Integer, TreeNode> tree;
	private HashMap<Integer, TreeHierachyModel> abstractMap;
	private HashMap<Integer, Specimen> specimenTaxonomy;
	private TreeHierachyModel abstractTree;
	private List<Taxonomy> allTaxonomys;
	private List<Specimen> allSpecimens;
	private List<TaxonomyLevel> allTaxonomyLevels;
	private List<TaxonomyLevel> avalLevels;

	private List<Specimen> taxonomySpecimens;

	private static final Logger LOGGER = Logger.getLogger(TaxonomyBean.class.getSimpleName());

	public TaxonomyBean() throws Exception {
		specimenService = new DataBaseService<>(Specimen.class, Constant.UNLIMITED_QUERY_RESULTS);
		taxonomyService = new DataBaseService<>(Taxonomy.class, Constant.UNLIMITED_QUERY_RESULTS);
		taxonomyLevelService = new DataBaseService<>(TaxonomyLevel.class, Constant.UNLIMITED_QUERY_RESULTS);
	}

	@PostConstruct
	public void init() {
		try {
			allTaxonomys = taxonomyService.getList();
			allTaxonomyLevels = taxonomyLevelService.getList();
			allSpecimens = specimenService.getList();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
		createTaxTree();
	}

	public void persist() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.CREATE_ERROR;
		String transactionMessage = taxonomy.getTaxonomyName();
		try {
			taxonomy.setIdContainer(new Taxonomy(parentTaxonomy.getIdTaxonomy()));
			taxonomy.setIdTaxlevel(new TaxonomyLevel(new Integer(selectedLevel)));
			taxonomy = taxonomyService.persist(taxonomy);
			if (taxonomy != null && taxonomy.getIdTaxonomy() != null) {
				allTaxonomys.add(taxonomy);
				outcomeEnum = OutcomeEnum.CREATE_SUCCESS;
				createTaxTree();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error persisting", e);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
		selectedLevel = null;
	}

	public void edit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.UPDATE_ERROR;
		String transactionMessage = taxonomy.getTaxonomyName();
		try {
			Taxonomy tempTaxonomy = taxonomyService.merge(taxonomy);
			allTaxonomys.remove(taxonomy);
			allTaxonomys.add(tempTaxonomy);
			outcomeEnum = OutcomeEnum.UPDATE_SUCCESS;
			createTaxTree();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error editing", e);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
	}

	public void delete() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.DELETE_ERROR;
		String transactionMessage = taxonomy.getTaxonomyName();
		try {
			taxonomyService.delete(taxonomy);
			allTaxonomys.remove(taxonomy);
			createTaxTree();
			outcomeEnum = OutcomeEnum.DELETE_SUCCESS;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error deleting", e);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
	}

	public void setTaxonomyTree() {
		taxonomy = (Taxonomy) selectedNode.getData();
	}

	public void prepareCreate() {
		parentTaxonomy = taxonomy;
		taxonomy = new Taxonomy();
		selectedLevel = null;
		avalLevels = new ArrayList<TaxonomyLevel>();

		for (TaxonomyLevel t : allTaxonomyLevels) {
			if (t.getTaxlevelRank() > parentTaxonomy.getIdTaxlevel().getTaxlevelRank()) {
				avalLevels.add(t);
			}
		}
	}

	private void updateAvalLevels(Taxonomy tax, String command) {
		selectedLevel = tax.getIdTaxlevel().getIdTaxlevel().toString();
		avalLevels = new ArrayList<>();
		for (TaxonomyLevel t : allTaxonomyLevels) {
			if (Constant.CREATE_COMMAND.equals(command)) {
				if (t.getTaxlevelRank() > tax.getIdTaxlevel().getTaxlevelRank())
					avalLevels.add(t);
			} else if (t.getTaxlevelRank() >= tax.getIdTaxlevel().getTaxlevelRank())
				avalLevels.add(t);
		}
	}

	private void createTaxTree() {
		tree = new HashMap<>();
		abstractMap = new HashMap<>();
		TreeHierachyModel fatherNode = new TreeHierachyModel();
		TreeHierachyModel childNode = new TreeHierachyModel();
		root = null;
		if (allTaxonomys != null) {
			for (Taxonomy t : allTaxonomys) {
				if (root == null) {
					abstractTree = new TreeHierachyModel(t.getIdTaxonomy());
					tree.put(t.getIdTaxonomy(), (root = new DefaultTreeNode(t, null)));
					abstractMap.put(t.getIdTaxonomy(), abstractTree);
				} else {
					childNode = new TreeHierachyModel(t.getIdTaxonomy());
					fatherNode = abstractMap.get(t.getIdContainer().getIdTaxonomy());
					fatherNode.addNode(childNode);
					abstractMap.put(t.getIdTaxonomy(), childNode);
					tree.put(t.getIdTaxonomy(), new DefaultTreeNode(t, tree.get(t.getIdContainer().getIdTaxonomy())));
				}
			}

			TreeNode n = null;
			if (parentTaxonomy != null) {
				n = tree.get(parentTaxonomy.getIdTaxonomy());
			} else if (taxonomy != null) {
				n = tree.get(taxonomy.getIdTaxonomy());
			}
			openBranch(n);
		}

		if (allSpecimens != null) {
			specimenTaxonomy = new HashMap<>();
			for (Specimen s : allSpecimens) {
				specimenTaxonomy.put(s.getIdTaxonomy().getIdTaxonomy(), s);
			}
		}
	}

	public Taxonomy getNodeName() {
		return (Taxonomy) selectedNode.getData();
	}

	private void openBranch(TreeNode node) {
		if (node == null) {
			return;
		}
		deselectAll();
		node.setSelected(true);
		while (node != null) {
			node.setExpanded(true);
			node = node.getParent();
		}
	}

	public void deselectAll() {
		for (TreeNode t : tree.values()) {
			t.setSelected(false);
			t.setExpanded(false);
		}
	}

	public void selectNodeFromId(Integer idLocation) {
		selectedNode = tree.get(idLocation);
		openBranch(selectedNode);
	}

	public void setTaxfromNode(String command) {
		if (selectedNode != null) {
			try {
				root.setExpanded(true);
				taxonomy = (Taxonomy) selectedNode.getData();
				updateAvalLevels(taxonomy, command);
				if (Constant.CREATE_COMMAND.equals(command)) {
					parentTaxonomy = (Taxonomy) selectedNode.getData();
					taxonomy = new Taxonomy();
				} else if (Constant.DETAIL_COMMAND.equals(command)) {
					taxonomySpecimens = new ArrayList<>();
					Stack<TreeHierachyModel> searchList = new Stack<>();
					searchList.add(abstractMap.get(taxonomy.getIdTaxonomy()));
					TreeHierachyModel node = null;
					while (!searchList.isEmpty()) {
						node = searchList.pop();
						if (specimenTaxonomy.containsKey(node.getNode())) {
							taxonomySpecimens.add(specimenTaxonomy.get(node.getNode()));
						}
						searchList.addAll(node.getLeaves());
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error setting TaxFromNode", e);
			}
		}
	}

	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}

	public List<Taxonomy> getAllTaxonomys() {
		return allTaxonomys;
	}

	public String getSelectedLevel() {
		return selectedLevel;
	}

	public void setSelectedLevel(String selectedLevel) {
		this.selectedLevel = selectedLevel;
	}

	public List<TaxonomyLevel> getAllLevels() {
		return allTaxonomyLevels;
	}

	public TreeNode getTaxRoot() {
		return root;
	}

	public void setTaxRoot(TreeNode taxRoot) {
		this.root = taxRoot;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<Specimen> getTaxonomySpecimens() {
		return taxonomySpecimens;
	}

	public void setTaxonomySpecimens(List<Specimen> taxonomySpecimens) {
		this.taxonomySpecimens = taxonomySpecimens;
	}

	public List<TaxonomyLevel> getAvalLevels() {
		return avalLevels;
	}

	public void setAvalLevels(List<TaxonomyLevel> avalLevels) {
		this.avalLevels = avalLevels;
	}

	public Taxonomy getParentTaxonomy() {
		return parentTaxonomy;
	}

	public void setParentTaxonomy(Taxonomy parentTaxonomy) {
		this.parentTaxonomy = parentTaxonomy;
	}

}
