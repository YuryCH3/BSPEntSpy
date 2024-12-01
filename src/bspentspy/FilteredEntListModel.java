package bspentspy;

import java.util.*;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class FilteredEntListModel extends AbstractListModel<Entity> {
	private IFilter filter;
	private List<Entity> entities;
	private List<Entity> original;
	private ArrayList<Integer> originalIndices = new ArrayList<Integer>();
	private int[] indexMap = new int[0];
	private boolean activeSortByName = false;
	
	public List<Entity> getFilteredEntities(){
		return entities;
	}
	
	public void setFilter(IFilter filter) {
		IFilter prev = this.filter;
		this.filter = filter;
		
		if(filter != prev)
			applyFilterAndSort();
	}

	public void sortByName()
	{
		this.activeSortByName = !this.activeSortByName;
		applyFilterAndSort();
	}

	public int getSize() {
		if(entities == null)
			return 0;
		return entities.size();
	}
	
	public void setEntityList(List<Entity> ents) {
		original = ents;
		applyFilterAndSort();
		this.fireContentsChanged(this, 0, ents.size());
	}
	
	public void reload() {
		this.fireContentsChanged(this, 0, getSize());
	}
	
	public void reload(int start, int finish) {
		this.fireContentsChanged(this, start, finish);
	}

	@Override
	public Entity getElementAt(int index) {
		return entities.get(index);
	}
	
	//filtered index -> original index
	public int getIndexAt(int index) {
		if(index >= originalIndices.size() || index < 0)
			return -1;
		return originalIndices.get(index);
	}
	
	//original index -> filtered index, inverse of getIndexAt
	public int indexOf(int index) {
		if(index >= indexMap.length || index < 0)
			return -1;
		return indexMap[index];
	}
	
	public int[] translateIndices(int[] indices) {
		int[] newIndices = new int[indices.length];
		
		for(int i = 0; i < newIndices.length; ++i) {
			newIndices[i] = originalIndices.get(indices[i]);
		}
		
		return newIndices;
	}
	
	public int[] detranslateIndices(int[] indices) {
		int[] newIndices = new int[indices.length];
		
		int j = 0;
		for(int i = 0; i < newIndices.length; ++i) {
			newIndices[i] = indexMap[indices[i]];
			if(newIndices[i] > -1)
				++j;
		}
		
		return Arrays.copyOf(newIndices, j);
	}
	
	private void filter() {
		// filtered: 		index -> ent
		// originalIndices: filt ind -> orig ind
		// indexMap: 		orig ind -> filt ind

		ArrayList<Entity> filtered = new ArrayList<Entity>();

		originalIndices.clear();
		indexMap = new int[original.size()]; // allows getting index of filteted by index of original
		
		for(int i = 0; i < original.size(); ++i) {
			indexMap[i] = -1;
			if(filter == null || filter.match(original.get(i))) {
				originalIndices.add(i);
				indexMap[i] = filtered.size();
				filtered.add(original.get(i));
			}
		}
		
		entities = filtered;
		this.fireContentsChanged(this, 0, getSize());
	}

	private void sort()
	{
		ArrayList<Triplet<String, Integer, Entity>> sortedEnts = new ArrayList<Triplet<String, Integer, Entity>>();

		for (int i = 0; i < entities.size(); ++i)
		{
			sortedEnts.add(new Triplet<>(entities.get(i).toString(), originalIndices.get(i), entities.get(i)));
		}

		sortedEnts.sort(new Comparator<Triplet<String, Integer, Entity>>() {
			@Override
			public int compare(Triplet<String, Integer, Entity> o1, Triplet<String, Integer, Entity> o2) {
				if (o1.first != null && o2.first != null && !o1.first.equals(o2.first))
					return o1.first.compareTo(o2.first);
				return o1.second.compareTo(o2.second);
			}
		});

		int[] indexReMap = new int[original.size()];
        Arrays.fill(indexReMap, -1);

		for (int i = 0; i < originalIndices.size(); ++i)
		{
			indexReMap[originalIndices.get(i)] = i;
		}
		indexMap = indexReMap;

		ArrayList<Entity> sorted = new ArrayList<Entity>();
		originalIndices.clear();
		for (var nameIndEnt : sortedEnts)
		{
			sorted.add(nameIndEnt.third);
			originalIndices.add(nameIndEnt.second);
		}
		entities = sorted;

		this.fireContentsChanged(this, 0, getSize());
	}

	private void applyFilterAndSort()
	{
		if (this.activeSortByName)
		{
			filter();
			sort();
		}
		else
		{
			filter();
		}
	}
}
