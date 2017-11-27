package com.technicalitiesmc.lib;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import elec332.core.json.JsonHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for anything json-related.
 */
public final class JSONUtils {

    private JSONUtils() {
    }

    private static final Gson GSON = JsonHandler.getGson();

    /**
     * Reads a JSON file from an {@link InputStream} and creates an object of the specified type.
     */
    public static <T> T read(InputStream is, Class<T> clazz) throws JsonIOException, JsonSyntaxException {
        return GSON.fromJson(new InputStreamReader(is), clazz);
    }

    /**
     * Writes a JSON element to an {@link OutputStream}.
     */
    public static void write(OutputStream os, JsonElement element) throws JsonIOException, JsonSyntaxException, IOException {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(os))) {
            writer.setLenient(true);
            writer.setIndent("  ");
            GSON.toJson(element, writer);
        }
    }

    public static <T> T get(JsonObject object, String name, Class<T> type) throws JsonParseException {
        return get(object, name, type, "Could not find %s", "Attempted to parse value as a " + type.getName() + " but found %s");
    }

    public static <T> T get(JsonObject object, String name, Class<T> type, String unexpectedTypeException) throws JsonParseException {
        return get(object, name, type, "Could not find %s", unexpectedTypeException);
    }

    public static <T> T get(JsonObject object, String name, Class<T> type, String missingException, String unexpectedTypeException)
            throws JsonParseException {
        if (!object.has(name)) {
            throw new JsonParseException(String.format(missingException, name));
        }

        JsonElement element = object.get(name);
        if (element.isJsonNull()) {
            return null;
        }

        return as(element, type, unexpectedTypeException);
    }

    public static <T> T as(JsonElement element, Class<T> type) throws JsonParseException {
        return as(element, type, "Attempted to parse value as a " + type.getName() + " but found %s");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T as(JsonElement element, Class<T> type, String unexpectedTypeException) throws JsonParseException {
        if (type == JsonElement.class) {
            return (T) element;
        } else if (type == JsonObject.class) {
            if (!element.isJsonObject()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }
            return (T) element.getAsJsonObject();
        } else if (type == JsonArray.class) {
            if (!element.isJsonArray()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }
            return (T) element.getAsJsonArray();
        } else if (type == JsonPrimitive.class || type == String.class || type.isPrimitive() || type == Integer.class || type == Float.class
                || type == Boolean.class) {
            if (!element.isJsonPrimitive()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            if (type == JsonPrimitive.class) {
                return (T) element.getAsJsonPrimitive();
            }

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (type == Integer.TYPE || type == Integer.class) {
                if (!primitive.isNumber()) {
                    throw new JsonParseException(String.format(unexpectedTypeException, element));
                }
                return (T) Integer.valueOf(primitive.getAsInt());
            } else if (type == Float.TYPE || type == Float.class) {
                if (!primitive.isNumber()) {
                    throw new JsonParseException(String.format(unexpectedTypeException, element));
                }
                return (T) Float.valueOf(primitive.getAsFloat());
            } else if (type == Boolean.TYPE || type == Boolean.class) {
                if (!primitive.isBoolean()) {
                    throw new JsonParseException(String.format(unexpectedTypeException, element));
                }
                return (T) Boolean.valueOf(primitive.getAsBoolean());
            } else if (type == String.class) {
                if (!primitive.isString()) {
                    throw new JsonParseException(String.format(unexpectedTypeException, element));
                }
                return (T) primitive.getAsString();
            }
        } else if (type.isEnum()) {
            if (!element.isJsonPrimitive()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isString()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            return (T) Enum.valueOf((Class<Enum>) type, primitive.getAsString());
        } else if (type == Vec3i.class || type == BlockPos.class) {
            if (!element.isJsonArray()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            List<Integer> values = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                    int val = e.getAsInt();
                    if (val > 0) {
                        values.add(val);
                    } else {
                        throw new JsonParseException("Vectors must be expressed as an array of 3 numbers.");
                    }
                } else {
                    throw new JsonParseException("Vectors must be expressed as an array of 3 numbers.");
                }
            }

            return (T) (type == Vec3i.class ? new Vec3i(values.get(0), values.get(1), values.get(2))
                    : new BlockPos(values.get(0), values.get(1), values.get(2)));
        } else if (type == Vector3f.class) {
            if (!element.isJsonArray()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            List<Float> values = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                    float val = e.getAsFloat();
                    if (val > 0) {
                        values.add(val);
                    } else {
                        throw new JsonParseException("Vectors must be expressed as an array of 3 numbers.");
                    }
                } else {
                    throw new JsonParseException("Vectors must be expressed as an array of 3 numbers.");
                }
            }

            return (T) new Vector3f(values.get(0), values.get(1), values.get(2));
        } else if (type == Matrix4f.class) {
            if (!element.isJsonObject()) {
                throw new JsonParseException(String.format(unexpectedTypeException, element));
            }

            JsonObject obj = element.getAsJsonObject();
            Matrix4f matrix = new Matrix4f();
            matrix.setIdentity();
            if (obj.has("rotation")) {
                JsonArray rotation = get(obj, "rotation", JsonArray.class,
                        "Rotations must be expressed as an array of rotation definitions.");
                for (JsonElement e : rotation.getAsJsonArray()) {
                    JsonObject rot = as(e, JsonObject.class, "Rotation definitions must be defined by an axis and an amount.");

                    EnumFacing.Axis axis = get(rot, "axis", EnumFacing.Axis.class, "Rotation definitions must have an axis.",
                            "Found invalid rotation axis: %s");
                    if (axis == null) {
                        throw new JsonParseException("Found invalid rotation axis: " + rot.get("axis"));
                    }
                    float amount = get(rot, "amount", float.class, "Rotation definitions must have an amount.",
                            "Found invalid rotation amount: %s");

                    Matrix4f rotationMatrix = new Matrix4f();
                    rotationMatrix.setIdentity();
                    switch (axis) {
                        case X:
                            rotationMatrix.rotX((float) Math.toRadians(amount));
                            break;
                        case Y:
                            rotationMatrix.rotY((float) Math.toRadians(amount));
                            break;
                        case Z:
                            rotationMatrix.rotZ((float) Math.toRadians(amount));
                            break;
                        default:
                            break;
                    }
                    matrix.mul(rotationMatrix);
                }
            }
            if (obj.has("translation")) {
                Vector3f translation = get(obj, "translation", Vector3f.class, "Translations must be expressed as an array of 3 numbers.");

                Matrix4f translationMatrix = new Matrix4f();
                translationMatrix.setIdentity();
                translationMatrix.setTranslation(translation);
                matrix.mul(translationMatrix);
            }
            return (T) matrix;
        }

        throw new IllegalArgumentException("Invalid type: " + type.getName());
    }

    public static Matrix4f parseModelRotation(JsonObject object) throws JsonParseException {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        Vector3f center = object.has("center") ? get(object, "center", Vector3f.class) : new Vector3f(8, 8, 8);
        center.scale(1 / 16F);
        EnumFacing.Axis axis = get(object, "axis", Axis.class);
        float angle = get(object, "angle", float.class);

        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(center);
        matrix.mul(translation);

        Matrix4f rotation = new Matrix4f();
        rotation.setIdentity();
        switch (axis) {
            case X:
                rotation.rotX((float) Math.toRadians(angle));
                break;
            case Y:
                rotation.rotY((float) Math.toRadians(angle));
                break;
            case Z:
                rotation.rotZ((float) Math.toRadians(angle));
                break;
            default:
                break;
        }
        matrix.mul(rotation);

        center.negate();
        translation.setIdentity();
        translation.setTranslation(center);
        matrix.mul(translation);

        return matrix;
    }

}
