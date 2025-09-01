package bspentspy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

import bspentspy.Entity.KeyValue;

public abstract class BSPFile implements AutoCloseable{
	public boolean entDirty;
	protected RandomAccessFile bspfile;
	protected ArrayList<Entity> entities;
	protected HashMap<String, ArrayList<Entity>> links;
	
	protected BSPFile() {
		entities = new ArrayList<Entity>();
		entDirty = false;
		links = new HashMap<String, ArrayList<Entity>>();
	}
	
	public ArrayList<Entity> getEntities(){
		return entities;
	}
	
	public ArrayList<Entity> getLinkedEntities(Entity e){
		return links.get(e.targetname);
	}
	
	public void updateLinks() {
		links.clear();
		
		HashSet<String> nameSet = new HashSet<String>();
		for(Entity e : entities) {
			if(e.targetname == null || e.targetname.isEmpty())
				continue;
			
			nameSet.add(e.targetname);
			links.put(e.targetname, new ArrayList<Entity>());
		}
		for(Entity e : entities) {
			for(KeyValue kv : e.keyvalues) {
				if(kv.key.equals("targetname"))
					continue;
				String target = kv.getTarget();
				
				if(nameSet.contains(target)) {
					links.get(target).add(e);
				}
			}
		}
	}
	
	public void close() throws IOException {
		if(bspfile == null)
			return;
		bspfile.close();
		bspfile = null;
	}
	
	protected void copy(RandomAccessFile out, GenericLump from, GenericLump to) throws IOException {
		int buffSize = 20480;
		
		byte[] block = new byte[Math.min(buffSize, (int)to.length)];
		int blocks = (int)to.length / block.length;
		int remainder = (int)(to.length - blocks * block.length);
		
		bspfile.seek(from.offset);
		out.seek(to.offset);
		
		for(int i = 0; i < blocks; ++i) {
			bspfile.read(block);
			out.write(block);
		}
		
		if(remainder == 0)
			return;
		
		bspfile.read(block, 0, remainder);
		out.write(block, 0, remainder);
	}
	
	public void copy(RandomAccessFile out) throws IOException{
		byte[] block = new byte[20480];
		long blocks = out.length() / block.length;
		long remainder = out.length() % block.length;
		
		out.seek(0);
		bspfile.seek(0);
		for(long i = 0; i < blocks; ++i) {
			bspfile.read(block);
			out.write(block);
		}
		
		if(remainder > 0) {
			bspfile.read(block, 0, (int)remainder);
			out.write(block, 0, (int)remainder);
		}
		
		out.setLength(out.length());
	}
	
	protected byte[] getEntityBytes() throws IOException {		
		StringBuilder sb = new StringBuilder();
		
		if(!entities.get(0).classname.equals("worldspawn")) {
			for(int i = 0; i < entities.size(); ++i) {
				if(!entities.get(i).classname.equals("worldspawn"))
					continue;
				Entity worldspawn = entities.get(i);
				entities.remove(i);
				entities.add(0, worldspawn);
				break;
			}
		}
		
		for(Entity e : entities) {
			if(!e.shouldSave())
				continue;
			sb.append("{\n");
			for(int i = 0; i < e.keyvalues.size(); ++i) {
				sb.append("\"").append(e.keyvalues.get(i).key).append("\" \"").append(e.keyvalues.get(i).value).append("\"\n");
			}
			sb.append("}\n");
		}
		
		return sb.append('\0').toString().getBytes(StandardCharsets.UTF_8);
	} 
	
	public abstract boolean read(RandomAccessFile in) throws IOException;
	public abstract void save(RandomAccessFile out, boolean updateSelf) throws IOException;
	
	public static long alignToFour(long offset) {
		return ((long)(offset + 3) / 4) * 4;
	}
	
	public static BSPFile readFile(RandomAccessFile bspfile) throws IOException {
		BSPFile[] supported = {new SourceBSPFile(), new GoldSrcBSPFile()};
		
		for(BSPFile f : supported) {
			if(f.read(bspfile)) {
				return f;
			}
		}
		
		return null;
	}
	
	protected void readEntities(BufferedReader br) throws IOException {
		String line;
		Entity currEnt = null;
		while((line = br.readLine()) != null) {
			if (line.equals("{")) {
				currEnt = new Entity();
				currEnt.index = entities.size();
				continue;
			}
			
			if(line.equals("}")) {
				entities.add(currEnt);
				currEnt = null;
				continue;
			}
			
			String[] fields = line.split("\"", -1);
			if (fields.length == 5) {
				String ckey = fields[1];
				String cval = fields[3];
				//seems like commas are replaced with ESC character in newer versions of BSP (Gmod, TF2)
				currEnt.addKeyVal(ckey, cval);
			}
		}
	}
	
	public static class GenericLump implements Comparable<GenericLump>, Cloneable{
		int index;
		long offset;
		long length;

		public int compareTo(GenericLump o) {
			return Long.compare(this.offset, o.offset);
		}
		
		public Object clone() {
			GenericLump clone = new GenericLump();
			clone.index = index;
			clone.offset = offset;
			clone.length = length;
			
			return clone;
		}
		
		public String toString() {
			return "index: " + index + String.format("\toffset: %,d\tlen: %,d", offset, length);
		}
	}

	public void printStatsLight() {

		ArrayList<String> targetEntNames = new ArrayList<>();
		targetEntNames.add("light_environment");
		targetEntNames.add("env_sun");
		targetEntNames.add("light");
		targetEntNames.add("light_spot");
		targetEntNames.add("env_sprite");
		targetEntNames.add("point_spotlight");

		ArrayList<String> targetProptertyNames = new ArrayList<>();
		targetProptertyNames.add("_lightscaleHDR");
		targetProptertyNames.add("_AmbientScaleHDR");
		targetProptertyNames.add("HDRColorScale");

		var stats = collectStats(targetEntNames, targetProptertyNames, false);
		printStats(targetEntNames, stats);
	}

	public void printStatsSprops() {

		ArrayList<String> targetEntNames = new ArrayList<>();
		targetEntNames.add("prop_static");

		ArrayList<String> targetProptertyNames = new ArrayList<>();
		targetProptertyNames.add("disablevertexlighting");
//		targetProptertyNames.add("disableshadows");

		var stats = collectStats(targetEntNames, targetProptertyNames, true);
		printStats(targetEntNames, stats);

		ArrayList<String> targetProptertyNames2 = new ArrayList<>();
//		targetProptertyNames2.add("disablevertexlighting");
		targetProptertyNames2.add("disableshadows");

		var flagStats = collectFlagStats(targetEntNames, targetProptertyNames2);
		printFlagStats(targetEntNames, flagStats);
	}

	private TreeMap<String, TreeMap<String, ArrayList<Double>>>
		collectFlagStats(
				ArrayList<String> targetEntNames,
				ArrayList<String> targetPropertyNames
	)
	{
		var stats = new TreeMap<String, TreeMap<String, ArrayList<Double>>>();

		for (var ent : entities)
		{
			if (!targetEntNames.contains(ent.classname))
				continue;

			for (String propertyName : targetPropertyNames)
			{
				String val = ent.getKeyValue(propertyName);
				if (val.isBlank())
					continue;

				if (!stats.containsKey(propertyName))
					stats.put(propertyName, new TreeMap<>());
				var propStats = stats.get(propertyName);

				try
				{
					Double valf = Double.parseDouble(val);

					if (!propStats.containsKey(ent.modelname))
						propStats.put(ent.modelname, new ArrayList<>());
					var entStats = propStats.get(ent.modelname);

					entStats.add(valf);
				}
				catch (Exception ex)
				{
					System.err.println("Failed to parse " + ent.classname + "." + propertyName + ": " + "\"" + val + "\";\n" + ex);
				}
			}
		}

		return stats;
	}

	private TreeMap<String, TreeMap<String, ArrayList<Double>>>
	collectStats(
			ArrayList<String> targetEntNames,
			ArrayList<String> targetPropertyNames,
			boolean skipzeroes
	)
	{
		var stats = new TreeMap<String, TreeMap<String, ArrayList<Double>>>();

		for (var ent : entities)
		{
			if (!targetEntNames.contains(ent.classname))
				continue;

			if (!stats.containsKey(ent.classname))
				stats.put(ent.classname, new TreeMap<>());

			var entStats = stats.get(ent.classname);

			for (String propertyName : targetPropertyNames)
			{
				String val = ent.getKeyValue(propertyName);
				if (val.isBlank())
					continue;

				if (val.equals("0"))
					if (!skipzeroes)
						continue;

				try
				{
					Double valf = Double.parseDouble(val);

					if (!entStats.containsKey(propertyName))
						entStats.put(propertyName, new ArrayList<>());

					var propStats = entStats.get(propertyName);
					propStats.add(valf);
				}
				catch (Exception ex)
				{
					System.err.println("Failed to parse " + ent.classname + "." + propertyName + ": " + "\"" + val + "\";\n" + ex);
				}
			}
		}

		return stats;
	}

	private static void printFlagStats(ArrayList<String> targetEntNames, TreeMap<String, TreeMap<String, ArrayList<Double>>> stats)
	{
		for (var item : stats.entrySet())
		{
			var propName = item.getKey();
			var entDict = item.getValue();

			System.out.println();
			System.out.format("%s", propName);
			System.out.println();

			for (var item2 : entDict.entrySet())
			{
				var modelName = item2.getKey();
				var values = item2. getValue();

				Double avgVal = values.stream().mapToDouble(a -> a).average().getAsDouble();
				if (avgVal == 0)
					continue;

				System.out.format("%34s", modelName);
				System.out.format("\t%.2f", avgVal);
				System.out.print("\tx "); System.out.print(values.size());

				System.out.println();

			}
		}
	}

	private static void printStats(ArrayList<String> targetEntNames, TreeMap<String, TreeMap<String, ArrayList<Double>>> stats) {
		for (var entName : targetEntNames)
		{
			var entStats = stats.get(entName);
			if (entStats == null)
			{
//				System.out.format("%17s", entName);
//				System.out.println();
				continue;
			}

			System.out.format("%17s", entName);
			if (entStats.isEmpty())
			{
				System.out.println();
				continue;
			}

			boolean first = true;
			for (var propStats : entStats.entrySet())
			{
				System.out.print(" ");
				String fmt = first ? "%16s" : "%33s";
				first = false;
				System.out.format(fmt, propStats.getKey());
				var values = propStats.getValue();

				Double minVal = Collections.min(values);
				Double maxVal = Collections.max(values);
				Double avgVal = values.stream().mapToDouble(a -> a).average().getAsDouble();

				if (values.size() == 1)
				{
					System.out.format("\t%.1f", avgVal);
				}
				else
				{
					if (maxVal - minVal > 1e-3)
					{
						System.out.format("\t%.2f", avgVal);
						System.out.print("\tx "); System.out.print(values.size());
						System.out.print("\tmin: "); System.out.print(minVal);
						System.out.print("\tmax: "); System.out.print(maxVal);
					}
					else
					{
						System.out.format("\t%.1f", avgVal);
						System.out.print("\tx "); System.out.print(values.size());
					}
				}

				System.out.println();
			}
		}
	}
}
