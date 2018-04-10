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

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import net.hpclab.cev.entities.Catalog;
import net.hpclab.cev.entities.Collection;
import net.hpclab.cev.entities.Institution;
import net.hpclab.cev.entities.RegType;
import net.hpclab.cev.entities.SampleType;
import net.hpclab.cev.entities.Specimen;
import net.hpclab.cev.enums.ModulesEnum;
import net.hpclab.cev.enums.OutcomeEnum;
import net.hpclab.cev.enums.StatusEnum;
import net.hpclab.cev.model.TreeHierachyModel;
import net.hpclab.cev.services.AccessService;
import net.hpclab.cev.services.Constant;
import net.hpclab.cev.services.DataBaseService;
import net.hpclab.cev.services.DataWarehouse;
import net.hpclab.cev.services.ObjectRetriever;
import net.hpclab.cev.services.ParseExceptionService;

@ManagedBean
@ViewScoped
public class CollectionBean extends UtilsBean implements Serializable {

	private static final long serialVersionUID = -7407272469474484227L;
	private DataBaseService<Collection> collectionService;
	private DataBaseService<Catalog> catalogService;
	private Constant.CollectionClassType classType;
	private Integer objectId;
	private String selectedInstitution;
	private String selectedCollection;
	private String selectedCatalog;
	private String objectType;
	private String objectName;
	private String objectFatherType;
	private String objectFatherName;
	private Catalog catalog;
	private Collection collection;
	private Integer sampleTypeId;
	private Integer regTypeId;
	private TreeNode root;
	private TreeNode selectedNode;
	private HashMap<Integer, TreeNode> tree;
	private HashMap<Integer, TreeHierachyModel> abstractMap;
	private HashMap<Integer, Specimen> specimenCollection;
	private TreeHierachyModel abstractTree;
	private List<Collection> avalCollections;
	private List<Catalog> avalCatalogs;
	private List<Specimen> collectionSpecimens;

	private static final Logger LOGGER = Logger.getLogger(CollectionBean.class.getSimpleName());

	public CollectionBean() throws Exception {
		collectionService = new DataBaseService<>(Collection.class);
		catalogService = new DataBaseService<>(Catalog.class);
	}

	@PostConstruct
	public void init() {
		try {
			limpiarFiltros();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
	}

	public void limpiarFiltros() {
		classType = null;
		objectId = null;
		selectedInstitution = null;
		selectedCollection = null;
		selectedCatalog = null;
		objectType = null;
		objectName = null;
		objectFatherType = null;
		objectFatherName = null;
		catalog = null;
		collection = null;
		sampleTypeId = null;
		regTypeId = null;
		root = null;
		selectedNode = null;
		selectedNode = null;
		root = null;
		createTree();
	}

	public void persist() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.CREATE_ERROR;
		String transactionMessage = objectName;
		try {
			if (!AccessService.getInstance().checkUserAccess(ModulesEnum.COLLECTION, getUsers(facesContext),
					Constant.AccessLevel.INSERT)) {
				showAccessMessage(facesContext, OutcomeEnum.INSERT_NOT_GRANTED);
				return;
			}

			switch (classType) {
			case INSTITUTION:
				collection = new Collection();
				collection.setCollectionName(objectName);
				collection.setStatus(StatusEnum.ACTIVO.get());
				collection.setIdInstitution((Institution) tree.get(objectId).getData());
				collection = collectionService.persist(collection);
				DataWarehouse.getInstance().allCollections.add(collection);
				objectId = Constant.COLLECTION_HINT + collection.getIdCollection();
				break;
			case COLLECTION:
				catalog = new Catalog();
				catalog.setCatalogName(objectName);
				catalog.setStatus(StatusEnum.ACTIVO.get());
				catalog.setIdCollection((Collection) tree.get(objectId).getData());
				catalog = catalogService.persist(catalog);
				DataWarehouse.getInstance().allCatalogs.add(catalog);
				objectId = Constant.CATALOG_HINT + catalog.getIdCatalog();
				break;
			case CATALOG:
				transactionMessage = "No es posible crear otros elementos";
				throw new Exception();
			}
			createTree();
			openBranch(tree.get(objectId));
			outcomeEnum = OutcomeEnum.CREATE_SUCCESS;
		} catch (Exception e) {
			transactionMessage = ParseExceptionService.getInstance().parse(e);
			LOGGER.log(Level.SEVERE, "Error persisting: " + transactionMessage);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
	}

	public void edit() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.UPDATE_ERROR;
		String transactionMessage = null;
		try {
			if (!AccessService.getInstance().checkUserAccess(ModulesEnum.COLLECTION, getUsers(facesContext),
					Constant.AccessLevel.UPDATE)) {
				showAccessMessage(facesContext, OutcomeEnum.UPDATE_NOT_GRANTED);
				return;
			}

			switch (classType) {
			case INSTITUTION:
				transactionMessage = "No es posible editar las instituciones";
				throw new Exception();
			case COLLECTION:
				transactionMessage = collection.getCollectionName();
				collection.setCollectionName(objectName);
				Collection tempCollection = collectionService.merge(collection);
				objectId = Constant.COLLECTION_HINT + tempCollection.getIdCollection();
				DataWarehouse.getInstance().allCollections.remove(collection);
				DataWarehouse.getInstance().allCollections.add(tempCollection);
				break;
			case CATALOG:
				transactionMessage = catalog.getCatalogName();
				catalog.setCatalogName(objectName);
				Catalog tempCatalog = catalogService.merge(catalog);
				objectId = Constant.CATALOG_HINT + tempCatalog.getIdCatalog();
				DataWarehouse.getInstance().allCatalogs.remove(catalog);
				DataWarehouse.getInstance().allCatalogs.add(tempCatalog);
				break;
			}
			outcomeEnum = OutcomeEnum.UPDATE_SUCCESS;
			createTree();
			openBranch(tree.get(objectId));
		} catch (Exception e) {
			transactionMessage = ParseExceptionService.getInstance().parse(e);
			LOGGER.log(Level.SEVERE, "Error editing: " + transactionMessage);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
	}

	public void delete() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		OutcomeEnum outcomeEnum = OutcomeEnum.DELETE_ERROR;
		String transactionMessage = null;
		try {
			if (!AccessService.getInstance().checkUserAccess(ModulesEnum.COLLECTION, getUsers(facesContext),
					Constant.AccessLevel.DELETE)) {
				showAccessMessage(facesContext, OutcomeEnum.DELETE_NOT_GRANTED);
				return;
			}

			switch (classType) {
			case INSTITUTION:
				break;
			case COLLECTION:
				transactionMessage = collection.getCollectionName();
				objectId = Constant.INSTITUTION_HINT + collection.getIdInstitution().getIdInstitution();
				collectionService.delete(collection);
				DataWarehouse.getInstance().allCollections.remove(collection);
				break;
			case CATALOG:
				transactionMessage = catalog.getCatalogName();
				objectId = Constant.COLLECTION_HINT + catalog.getIdCollection().getIdCollection();
				catalogService.delete(catalog);
				DataWarehouse.getInstance().allCatalogs.remove(catalog);
				break;
			}
			createTree();
			openBranch(tree.get(objectId));
			outcomeEnum = OutcomeEnum.DELETE_SUCCESS;
		} catch (Exception e) {
			transactionMessage = ParseExceptionService.getInstance().parse(e);
			LOGGER.log(Level.SEVERE, "Error deleting: " + transactionMessage);
		}
		showMessage(facesContext, outcomeEnum, transactionMessage);
	}

	public void createTree() {
		tree = new HashMap<>();
		abstractMap = new HashMap<>();
		TreeHierachyModel fatherNode = new TreeHierachyModel();
		TreeHierachyModel childNode = new TreeHierachyModel();
		root = new DefaultTreeNode("Colecciones", null);
		tree.put(0, root);
		abstractTree = new TreeHierachyModel(0, Constant.ROOT_LEVEL);
		abstractMap.put(0, abstractTree);
		for (Institution i : DataWarehouse.getInstance().allInstitutions) {
			childNode = new TreeHierachyModel(Constant.INSTITUTION_HINT + i.getIdInstitution(),
					Constant.INSTITUTION_LEVEL);
			fatherNode = abstractMap.get(0);
			fatherNode.addNode(childNode);
			abstractMap.put(Constant.INSTITUTION_HINT + i.getIdInstitution(), childNode);
			tree.put(Constant.INSTITUTION_HINT + i.getIdInstitution(), new DefaultTreeNode(i, tree.get(0)));
		}

		for (Collection c : DataWarehouse.getInstance().allCollections) {
			childNode = new TreeHierachyModel(Constant.COLLECTION_HINT + c.getIdCollection(),
					Constant.COLLECTION_LEVEL);
			fatherNode = abstractMap.get(Constant.INSTITUTION_HINT + c.getIdInstitution().getIdInstitution());
			fatherNode.addNode(childNode);
			abstractMap.put(Constant.COLLECTION_HINT + c.getIdCollection(), childNode);
			tree.put(Constant.COLLECTION_HINT + c.getIdCollection(), new DefaultTreeNode(c,
					tree.get(Constant.INSTITUTION_HINT + c.getIdInstitution().getIdInstitution())));
		}

		for (Catalog c : DataWarehouse.getInstance().allCatalogs) {
			childNode = new TreeHierachyModel(Constant.CATALOG_HINT + c.getIdCatalog(), Constant.CATALOG_LEVEL);
			fatherNode = abstractMap.get(Constant.COLLECTION_HINT + c.getIdCollection().getIdCollection());
			fatherNode.addNode(childNode);
			abstractMap.put(Constant.CATALOG_HINT + c.getIdCatalog(), childNode);
			tree.put(Constant.CATALOG_HINT + c.getIdCatalog(),
					new DefaultTreeNode(c, tree.get(Constant.COLLECTION_HINT + c.getIdCollection().getIdCollection())));
		}
		
		TreeNode n = null;
		if (catalog != null) {
			n = tree.get(Constant.CATALOG_HINT + catalog.getIdCatalog());
			collection = (Collection) n.getParent().getData();
		}
		
		openBranch(n);

		specimenCollection = new HashMap<>();
		for (Specimen s : DataWarehouse.getInstance().allSpecimens) {
			specimenCollection.put(Constant.CATALOG_HINT + s.getIdCatalog().getIdCatalog(), s);
		}
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

	public void onNodeSelect(NodeSelectEvent event) {
		selectedNode = event.getTreeNode();
		showMessage(FacesContext.getCurrentInstance(), OutcomeEnum.GENERIC_INFO,
				selectedNode.toString() + " seleccionado");
		if (selectedNode.getData() instanceof Catalog) {
			catalog = (Catalog) selectedNode.getData();
			collection = (Collection) selectedNode.getParent().getData();
		} else {
			catalog = null;
			collection = null;
		}
	}

	public void setDatafromNode(String command) {
		if (selectedNode != null) {
			try {
				root.setExpanded(true);
				classType = null;
				openBranch(selectedNode);
				if (selectedNode.getData() instanceof Institution) {
					classType = Constant.CollectionClassType.INSTITUTION;
					objectId = Constant.INSTITUTION_HINT + ((Institution) selectedNode.getData()).getIdInstitution();
					objectType = "Institución";
					objectName = ((Institution) selectedNode.getData()).getInstitutionName();
				} else if (selectedNode.getData() instanceof Collection) {
					classType = Constant.CollectionClassType.COLLECTION;
					collection = (Collection) selectedNode.getData();
					objectId = Constant.COLLECTION_HINT + collection.getIdCollection();
					objectType = "Colección";
					objectName = collection.getCollectionName();
				} else if (selectedNode.getData() instanceof Catalog) {
					catalog = (Catalog) selectedNode.getData();
					classType = Constant.CollectionClassType.CATALOG;
					objectId = Constant.CATALOG_HINT + catalog.getIdCatalog();
					objectType = "Catálogo";
					objectName = catalog.getCatalogName();
				}

				if (Constant.CREATE_COMMAND.equals(command)) {
					objectFatherName = objectName;
					objectName = "";
					switch (classType) {
					case INSTITUTION:
						objectFatherType = "Institución";
						objectType = "Colección";
						break;
					case COLLECTION:
						objectFatherType = "Colección";
						objectType = "Catalogo";
						break;
					case CATALOG:
						objectFatherType = "Catálogo";
						objectType = "";
						break;
					}
				} else if (Constant.DETAIL_COMMAND.equals(command)) {
					collectionSpecimens = new ArrayList<>();
					Stack<TreeHierachyModel> searchList = new Stack<>();
					searchList.add(abstractMap.get(objectId));
					TreeHierachyModel node = null;
					while (!searchList.isEmpty() && collectionSpecimens.size() <= Constant.MAX_SPECIMEN_LIST) {
						node = searchList.pop();
						if (specimenCollection.containsKey(node.getNode())) {
							collectionSpecimens.add(specimenCollection.get(node.getNode()));
						}
						searchList.addAll(node.getLeaves());
					}
					searchList = null;
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error setting DataFromNode", e);
			}
		}
	}

	public TreeNode getCollectionRoot() {
		if (selectedNode != null)
			openBranch(selectedNode);
		return root;
	}

	public void setCollectionRoot(TreeNode collectionRoot) {
		this.root = collectionRoot;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<Specimen> getCollectionSpecimens() {
		return collectionSpecimens;
	}

	public void setCollectionSpecimens(List<Specimen> collectionSpecimens) {
		this.collectionSpecimens = collectionSpecimens;
	}

	public SampleType getSampleType() {
		if (sampleTypeId != null)
			return ObjectRetriever.getObjectFromId(SampleType.class, sampleTypeId);
		return null;
	}

	public RegType getRegType() {
		if (regTypeId != null)
			return ObjectRetriever.getObjectFromId(RegType.class, regTypeId);
		return null;
	}

	public Integer getSampleTypeId() {
		return sampleTypeId;
	}

	public void setSampleTypeId(Integer sampleTypeId) {
		this.sampleTypeId = sampleTypeId;
	}

	public Integer getRegTypeId() {
		return regTypeId;
	}

	public void setRegTypeId(Integer regTypeId) {
		this.regTypeId = regTypeId;
	}

	public List<Institution> getAllInstitutions() {
		return DataWarehouse.getInstance().allInstitutions;
	}

	public List<Collection> getAllCollections() {
		return DataWarehouse.getInstance().allCollections;
	}

	public List<Catalog> getAllCatalogs() {
		return DataWarehouse.getInstance().allCatalogs;
	}

	public List<SampleType> getAllSampleTypes() {
		return DataWarehouse.getInstance().allSampleTypes;
	}

	public List<RegType> getAllRegTypes() {
		return DataWarehouse.getInstance().allRegTypes;
	}

	public String getSelectedInstitution() {
		return selectedInstitution;
	}

	public void setSelectedInstitution(String selectedInstitution) {
		this.selectedInstitution = selectedInstitution;
	}

	public String getSelectedCollection() {
		return selectedCollection;
	}

	public void setSelectedCollection(String selectedCollection) {
		this.selectedCollection = selectedCollection;
	}

	public String getSelectedCatalog() {
		return selectedCatalog;
	}

	public void setSelectedCatalog(String selectedCatalog) {
		this.selectedCatalog = selectedCatalog;
	}

	public List<Collection> getAvalCollections() {
		return avalCollections;
	}

	public void setAvalCollections(List<Collection> avalCollections) {
		this.avalCollections = avalCollections;
	}

	public List<Catalog> getAvalCatalogs() {
		return avalCatalogs;
	}

	public void setAvalCatalogs(List<Catalog> avalCatalogs) {
		this.avalCatalogs = avalCatalogs;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getObjectFatherName() {
		return objectFatherName;
	}

	public String getObjectFatherType() {
		return objectFatherType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setObjectFatherType(String objectFatherType) {
		this.objectFatherType = objectFatherType;
	}
}