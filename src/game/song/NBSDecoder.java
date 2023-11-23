package game.song;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class NBSDecoder {
	private static int id = 0;

	public static Song parse(File decodeFile) {
		try {
			return parse(Files.newInputStream(decodeFile.toPath(), new java.nio.file.OpenOption[0]), decodeFile);
		} catch (Exception e) {
			return null;
		}
	}

	private static Song parse(InputStream inputStream, File decodeFile) {
		id++;
		LinkedHashMap<Integer, Layer> layerHashMap = new LinkedHashMap<>();
		try {
			DataInputStream dis = new DataInputStream(inputStream);
			Short length = Short.valueOf(readShort(dis));
			Short songHeight = Short.valueOf(readShort(dis));
			String title = readString(dis);
			String author = readString(dis);
			readString(dis);
			String description = readString(dis);
			Float speed = Float.valueOf(readShort(dis) / 100.0F);
			dis.readBoolean();
			dis.readByte();
			dis.readByte();
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readInt(dis);
			readString(dis);
			short tick = -1;
			label26: while (true) {
				short jumpTicks = readShort(dis);
				if (jumpTicks == 0)
					break;
				tick = (short) (tick + jumpTicks);
				short layer = -1;
				while (true) {
					short jumpLayers = readShort(dis);
					if (jumpLayers == 0)
						continue label26;
					layer = (short) (layer + jumpLayers);
					setNote(layer, tick, Byte.valueOf(dis.readByte()), Byte.valueOf(dis.readByte()), layerHashMap);
				}
			}
			for (int i = 0; i < songHeight.shortValue(); i++) {
				Layer l = layerHashMap.get(Integer.valueOf(i));
				if (l != null) {
					l.setName(readString(dis));
					l.setVolume(dis.readByte());
				}
			}
			return new Song(id, speed.floatValue(), layerHashMap, songHeight.shortValue(), length.shortValue(), title,
					decodeFile.getName().replace(".nbs", ""), author, description, decodeFile);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private static void setNote(int layer, int ticks, Byte instrument, Byte key, HashMap<Integer, Layer> layerstore) {
		Layer l = layerstore.get(Integer.valueOf(layer));
		if (l == null) {
			l = new Layer();
			layerstore.put(Integer.valueOf(layer), l);
		}
		l.setNote(ticks, new Note(instrument, key));
	}

	private static short readShort(DataInputStream dis) throws IOException {
		int byte1 = dis.readUnsignedByte();
		int byte2 = dis.readUnsignedByte();
		return (short) (byte1 + (byte2 << 8));
	}

	private static int readInt(DataInputStream dis) throws IOException {
		int byte1 = dis.readUnsignedByte();
		int byte2 = dis.readUnsignedByte();
		int byte3 = dis.readUnsignedByte();
		int byte4 = dis.readUnsignedByte();
		return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
	}

	private static String readString(DataInputStream dis) throws IOException {
		int length = readInt(dis);
		StringBuilder sb = new StringBuilder(length);
		for (; length > 0; length--) {
			char c = (char) dis.readByte();
			if (c == '\r')
				c = ' ';
			sb.append(c);
		}
		return sb.toString();
	}
}
