package com.axialeaa.modid;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.level.material.MapColor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class ExampleModClient implements ClientModInitializer {

	private static final Map<String, String> REFERENCES = new Object2ObjectArrayMap<>();

	@Override
	public void onInitializeClient() {
		REFERENCES.clear();

		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("README.md"))) {
			printColorsWithReferences(writer);
			writer.write("\n");
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

	private static void printColorsWithReferences(OutputStreamWriter writer) throws IOException {
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

			String imageLink = "[<img valign='middle' src='https://readme-swatches.vercel.app/%s?style=round'/>][%s]";
			String reference = name.toLowerCase(Locale.ROOT).replace('_', '-');

			for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
				int argb = mapColor.calculateRGBColor(brightness);
				argb = argb & 0xFFFFFF;

				String color = toPaddedHexString(argb);
				String withBrightness = reference + "-" + brightness.toString().toLowerCase(Locale.ROOT);

				writer.write(imageLink.formatted(color, withBrightness));
				newLine(writer);

				REFERENCES.put(withBrightness, color);
			}

			writer.write("`MapColor.%s`".formatted(name));

			if (fields.indexOf(field) < fields.size() - 1)
				writer.write("<br>");

			newLine(writer);
		}
	}

	private static void printLinks(OutputStreamWriter writer) throws IOException {
		String link = "[%s]: https://www.colorhexa.com/%s";

		for (String ref : REFERENCES.keySet()) {
			String color = REFERENCES.get(ref);

			writer.write(link.formatted(ref, color));
			newLine(writer);
		}
	}

	private static void newLine(OutputStreamWriter writer) throws IOException {
		writer.write("\n");
	}

}