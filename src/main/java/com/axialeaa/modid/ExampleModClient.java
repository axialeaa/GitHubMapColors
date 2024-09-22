package com.axialeaa.modid;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.level.material.MapColor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class ExampleModClient implements ClientModInitializer {

	private static final String LINK = "[%s]: https://www.colorhexa.com/%s";
	private static final String IMAGE = "[<img valign='middle' src='https://readme-swatches.vercel.app/%s'/>][%s]";

	private static final Map<String, String> REFERENCES = new HashMap<>();

	@Override
	public void onInitializeClient() {
		REFERENCES.clear();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("README.md"))) {
			printMapColors(writer);
			printLinks(writer);
        } catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Field> getMapColorFields() {
		Field[] fields = MapColor.class.getDeclaredFields();

		Stream<Field> stream = Stream.of(fields);
		stream = stream.filter(field -> field.getType() == MapColor.class);

		return stream.toList();
	}

	private static String toPaddedHexString(int i) {
		return String.format("%06X", i);
	}

	private static void printMapColors(OutputStreamWriter writer) throws IOException {
		List<Field> fields = getMapColorFields();

		for (Field field : fields) {
			String name = field.getName();

			if (name.equals("NONE"))
				continue;

			MapColor mapColor;

			try {
				mapColor = (MapColor) field.get(MapColor.NONE);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			printColorsWithReferences(name, mapColor, writer);

			writer.write("`MapColor.%s`".formatted(name));

			if (fields.indexOf(field) < fields.size() - 1)
				writer.write("<br>");

			newLine(writer);
		}
	}

	private static void printColorsWithReferences(String fieldName, MapColor mapColor, OutputStreamWriter writer) throws IOException {
		for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
			int argb = mapColor.calculateRGBColor(brightness);
			argb = argb & 0xFFFFFF;

			String color = toPaddedHexString(argb);

			String formatted = "%s_%s".formatted(fieldName, brightness);
			String lowerCase = formatted.toLowerCase(Locale.ROOT).replace('_', '-');

			writer.write(IMAGE.formatted(color, lowerCase));
			REFERENCES.put(lowerCase, color);
		}
	}

	private static void printLinks(OutputStreamWriter writer) throws IOException {
		for (String ref : REFERENCES.keySet()) {
			String color = REFERENCES.get(ref);

			newLine(writer);
			writer.write(LINK.formatted(ref, color));
		}
	}

	private static void newLine(OutputStreamWriter writer) throws IOException {
		writer.write("\n");
	}

}