package game.song;

import java.util.LinkedHashMap;

public class Layer {
	private LinkedHashMap<Integer, Note> linkedHashMap = new LinkedHashMap<>();

	private byte volume = 100;

	private String name = "";

	public LinkedHashMap<Integer, Note> getLinkedHashMap() {
		return this.linkedHashMap;
	}

	public void setLinkedHashMap(LinkedHashMap<Integer, Note> linkedHashMap) {
		this.linkedHashMap = linkedHashMap;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Note getNote(int tick) {
		return this.linkedHashMap.get(Integer.valueOf(tick));
	}

	public void setNote(int tick, Note note) {
		this.linkedHashMap.put(Integer.valueOf(tick), note);
	}

	public byte getVolume() {
		return this.volume;
	}

	public void setVolume(byte volume) {
		this.volume = volume;
	}
}
