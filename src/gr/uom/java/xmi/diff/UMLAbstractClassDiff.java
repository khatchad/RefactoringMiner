package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringMinerTimedOutException;

import gr.uom.java.xmi.UMLAttribute;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.decomposition.OperationInvocation;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;

public abstract class UMLAbstractClassDiff {
	protected List<UMLOperation> addedOperations;
	protected List<UMLOperation> removedOperations;
	protected List<UMLAttribute> addedAttributes;
	protected List<UMLAttribute> removedAttributes;
	protected List<UMLOperationBodyMapper> operationBodyMapperList;
	protected List<UMLOperationDiff> operationDiffList;
	protected List<UMLAttributeDiff> attributeDiffList;
	protected List<Refactoring> refactorings;
	protected UMLModelDiff modelDiff;
	
	public UMLAbstractClassDiff(UMLModelDiff modelDiff) {
		this.addedOperations = new ArrayList<UMLOperation>();
		this.removedOperations = new ArrayList<UMLOperation>();
		this.addedAttributes = new ArrayList<UMLAttribute>();
		this.removedAttributes = new ArrayList<UMLAttribute>();
		this.operationBodyMapperList = new ArrayList<UMLOperationBodyMapper>();
		this.operationDiffList = new ArrayList<UMLOperationDiff>();
		this.attributeDiffList = new ArrayList<UMLAttributeDiff>();
		this.refactorings = new ArrayList<Refactoring>();
		this.modelDiff = modelDiff;		
	}

	public List<UMLOperation> getAddedOperations() {
		return addedOperations;
	}

	public List<UMLOperation> getRemovedOperations() {
		return removedOperations;
	}

	public List<UMLAttribute> getAddedAttributes() {
		return addedAttributes;
	}

	public List<UMLAttribute> getRemovedAttributes() {
		return removedAttributes;
	}

	public List<UMLOperationBodyMapper> getOperationBodyMapperList() {
		return operationBodyMapperList;
	}

	public List<UMLOperationDiff> getOperationDiffList() {
		return operationDiffList;
	}

	public List<UMLAttributeDiff> getAttributeDiffList() {
		return attributeDiffList;
	}

	public abstract void process() throws RefactoringMinerTimedOutException;
	
	protected abstract void checkForAttributeChanges() throws RefactoringMinerTimedOutException;

	protected abstract void createBodyMappers() throws RefactoringMinerTimedOutException;

	protected boolean isPartOfMethodMovedFromExistingMethod(UMLOperation removedOperation, UMLOperation addedOperation) {
		for(UMLOperationBodyMapper mapper : operationBodyMapperList) {
			List<OperationInvocation> invocationsCalledInOperation1 = mapper.getOperation1().getAllOperationInvocations();
			List<OperationInvocation> invocationsCalledInOperation2 = mapper.getOperation2().getAllOperationInvocations();
			Set<OperationInvocation> invocationsCalledOnlyInOperation1 = new LinkedHashSet<OperationInvocation>(invocationsCalledInOperation1);
			Set<OperationInvocation> invocationsCalledOnlyInOperation2 = new LinkedHashSet<OperationInvocation>(invocationsCalledInOperation2);
			invocationsCalledOnlyInOperation1.removeAll(invocationsCalledInOperation2);
			invocationsCalledOnlyInOperation2.removeAll(invocationsCalledInOperation1);
			for(OperationInvocation invocation : invocationsCalledOnlyInOperation2) {
				if(invocation.matchesOperation(addedOperation, mapper.getOperation2(), modelDiff)) {
					List<OperationInvocation> removedOperationInvocations = removedOperation.getAllOperationInvocations();
					List<OperationInvocation> addedOperationInvocations = addedOperation.getAllOperationInvocations();
					Set<OperationInvocation> movedInvocations = new LinkedHashSet<OperationInvocation>(addedOperationInvocations);
					movedInvocations.removeAll(removedOperationInvocations);
					movedInvocations.retainAll(invocationsCalledOnlyInOperation1);
					Set<OperationInvocation> intersection = new LinkedHashSet<OperationInvocation>(addedOperationInvocations);
					intersection.retainAll(removedOperationInvocations);
					int chainedCalls = 0;
					OperationInvocation previous = null;
					for(OperationInvocation inv : intersection) {
						if(previous != null && previous.getExpression() != null && previous.getExpression().equals(inv.actualString())) {
							chainedCalls++;
						}
						previous = inv;
					}
					if(movedInvocations.size() > 1 && intersection.size() - chainedCalls > 1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean isPartOfMethodMovedToExistingMethod(UMLOperation removedOperation, UMLOperation addedOperation) {
		for(UMLOperationBodyMapper mapper : operationBodyMapperList) {
			List<OperationInvocation> invocationsCalledInOperation1 = mapper.getOperation1().getAllOperationInvocations();
			List<OperationInvocation> invocationsCalledInOperation2 = mapper.getOperation2().getAllOperationInvocations();
			Set<OperationInvocation> invocationsCalledOnlyInOperation1 = new LinkedHashSet<OperationInvocation>(invocationsCalledInOperation1);
			Set<OperationInvocation> invocationsCalledOnlyInOperation2 = new LinkedHashSet<OperationInvocation>(invocationsCalledInOperation2);
			invocationsCalledOnlyInOperation1.removeAll(invocationsCalledInOperation2);
			invocationsCalledOnlyInOperation2.removeAll(invocationsCalledInOperation1);
			for(OperationInvocation invocation : invocationsCalledOnlyInOperation1) {
				if(invocation.matchesOperation(removedOperation, mapper.getOperation1(), modelDiff)) {
					List<OperationInvocation> removedOperationInvocations = removedOperation.getAllOperationInvocations();
					List<OperationInvocation> addedOperationInvocations = addedOperation.getAllOperationInvocations();
					Set<OperationInvocation> movedInvocations = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
					movedInvocations.removeAll(addedOperationInvocations);
					movedInvocations.retainAll(invocationsCalledOnlyInOperation2);
					Set<OperationInvocation> intersection = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
					intersection.retainAll(addedOperationInvocations);
					int chainedCalls = 0;
					OperationInvocation previous = null;
					for(OperationInvocation inv : intersection) {
						if(previous != null && previous.getExpression() != null && previous.getExpression().equals(inv.actualString())) {
							chainedCalls++;
						}
						previous = inv;
					}
					int renamedCalls = 0;
					for(OperationInvocation inv : addedOperationInvocations) {
						if(!intersection.contains(inv)) {
							for(Refactoring ref : refactorings) {
								if(ref instanceof RenameOperationRefactoring) {
									RenameOperationRefactoring rename = (RenameOperationRefactoring)ref;
									if(inv.matchesOperation(rename.getRenamedOperation(), addedOperation, modelDiff)) {
										renamedCalls++;
										break;
									}
								}
							}
						}
					}
					if(movedInvocations.size() > 1 && intersection.size() + renamedCalls - chainedCalls > 1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean isPartOfMethodExtracted(UMLOperation removedOperation, UMLOperation addedOperation) {
		List<OperationInvocation> removedOperationInvocations = removedOperation.getAllOperationInvocations();
		List<OperationInvocation> addedOperationInvocations = addedOperation.getAllOperationInvocations();
		Set<OperationInvocation> intersection = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
		intersection.retainAll(addedOperationInvocations);
		int numberOfInvocationsMissingFromRemovedOperation = new LinkedHashSet<OperationInvocation>(removedOperationInvocations).size() - intersection.size();
		
		Set<OperationInvocation> operationInvocationsInMethodsCalledByAddedOperation = new LinkedHashSet<OperationInvocation>();
		Set<OperationInvocation> matchedOperationInvocations = new LinkedHashSet<OperationInvocation>();
		for(OperationInvocation addedOperationInvocation : addedOperationInvocations) {
			if(!intersection.contains(addedOperationInvocation)) {
				for(UMLOperation operation : addedOperations) {
					if(!operation.equals(addedOperation) && operation.getBody() != null) {
						if(addedOperationInvocation.matchesOperation(operation, addedOperation, modelDiff)) {
							//addedOperation calls another added method
							operationInvocationsInMethodsCalledByAddedOperation.addAll(operation.getAllOperationInvocations());
							matchedOperationInvocations.add(addedOperationInvocation);
						}
					}
				}
			}
		}
		if(modelDiff != null) {
			for(OperationInvocation addedOperationInvocation : addedOperationInvocations) {
				String expression = addedOperationInvocation.getExpression();
				if(expression != null && !expression.equals("this") &&
						!intersection.contains(addedOperationInvocation) && !matchedOperationInvocations.contains(addedOperationInvocation)) {
					UMLOperation operation = modelDiff.findOperationInAddedClasses(addedOperationInvocation, addedOperation);
					if(operation != null) {
						operationInvocationsInMethodsCalledByAddedOperation.addAll(operation.getAllOperationInvocations());
					}
				}
			}
		}
		Set<OperationInvocation> newIntersection = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
		newIntersection.retainAll(operationInvocationsInMethodsCalledByAddedOperation);
		
		Set<OperationInvocation> removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
		removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.removeAll(intersection);
		removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.removeAll(newIntersection);
		for(Iterator<OperationInvocation> operationInvocationIterator = removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.iterator(); operationInvocationIterator.hasNext();) {
			OperationInvocation invocation = operationInvocationIterator.next();
			if(invocation.getMethodName().startsWith("get") || invocation.getMethodName().equals("add") || invocation.getMethodName().equals("contains")) {
				operationInvocationIterator.remove();
			}
		}
		int numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations = newIntersection.size();
		int numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations = numberOfInvocationsMissingFromRemovedOperation - numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations;
		return numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > numberOfInvocationsMissingFromRemovedOperationWithoutThoseFoundInOtherAddedOperations ||
				numberOfInvocationsOriginallyCalledByRemovedOperationFoundInOtherAddedOperations > removedOperationInvocationsWithIntersectionsAndGetterInvocationsSubtracted.size();
	}

	protected boolean isPartOfMethodInlined(UMLOperation removedOperation, UMLOperation addedOperation) {
		List<OperationInvocation> removedOperationInvocations = removedOperation.getAllOperationInvocations();
		List<OperationInvocation> addedOperationInvocations = addedOperation.getAllOperationInvocations();
		Set<OperationInvocation> intersection = new LinkedHashSet<OperationInvocation>(removedOperationInvocations);
		intersection.retainAll(addedOperationInvocations);
		int numberOfInvocationsMissingFromAddedOperation = new LinkedHashSet<OperationInvocation>(addedOperationInvocations).size() - intersection.size();
		
		Set<OperationInvocation> operationInvocationsInMethodsCalledByRemovedOperation = new LinkedHashSet<OperationInvocation>();
		for(OperationInvocation removedOperationInvocation : removedOperationInvocations) {
			if(!intersection.contains(removedOperationInvocation)) {
				for(UMLOperation operation : removedOperations) {
					if(!operation.equals(removedOperation) && operation.getBody() != null) {
						if(removedOperationInvocation.matchesOperation(operation, removedOperation, modelDiff)) {
							//removedOperation calls another removed method
							operationInvocationsInMethodsCalledByRemovedOperation.addAll(operation.getAllOperationInvocations());
						}
					}
				}
			}
		}
		Set<OperationInvocation> newIntersection = new LinkedHashSet<OperationInvocation>(addedOperationInvocations);
		newIntersection.retainAll(operationInvocationsInMethodsCalledByRemovedOperation);
		
		int numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations = newIntersection.size();
		int numberOfInvocationsMissingFromAddedOperationWithoutThoseFoundInOtherRemovedOperations = numberOfInvocationsMissingFromAddedOperation - numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations;
		return numberOfInvocationsCalledByAddedOperationFoundInOtherRemovedOperations > numberOfInvocationsMissingFromAddedOperationWithoutThoseFoundInOtherRemovedOperations;
	}
}
