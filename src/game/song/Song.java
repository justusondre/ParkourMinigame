package game.song;

import java.io.File;
import java.util.LinkedHashMap;

public class Song {
	private LinkedHashMap<Integer, Layer> layer;

	private short songHeight;

	private short length;

	private String title;

	private File path;

	private String name;

	private String author;

	private String description;

	private float speed;

	private int delay;

	private int frame = -1;

	private int tick = -40;

	private int id;

	public Song(Song other) {
		this.id = other.getID();
		this.speed = other.getSpeed();
		this.delay = Math.round(20.0F / this.speed);
		this.layer = other.getLayer();
		this.songHeight = other.getSongHeight();
		this.length = other.getLength();
		this.title = other.getTitle();
		this.author = other.getAuthor();
		this.description = other.getDescription();
		this.path = other.getPath();
		this.name = other.getName();
	}

	public Song(int id, float speed, LinkedHashMap<Integer, Layer> layer, short songHeight, short length, String title,
			String name, String author, String description, File path) {
		this.speed = speed;
		this.id = id;
		this.delay = Math.round(20.0F / speed);
		this.layer = layer;
		this.songHeight = songHeight;
		this.length = length;
		this.title = title;
		this.author = author;
		this.description = description;
		this.path = path;
		this.name = name;
	}

	public int getID() {
		return this.id;
	}

	public LinkedHashMap<Integer, Layer> getLayer() {
		return this.layer;
	}

	public short getSongHeight() {
		return this.songHeight;
	}

	public short getLength() {
		return this.length;
	}

	public String getTitle() {
		return this.title;
	}

	public int getTick() {
		return this.tick;
	}

	public int getFrame() {
		return this.frame;
	}

	public String getName() {
		return this.name;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public String getAuthor() {
		return this.author;
	}

	public File getPath() {
		return this.path;
	}

	public String getDescription() {
		return this.description;
	}

	public float getSpeed() {
		return this.speed;
	}

	public int getDelay() {
		return this.delay;
	}
}
