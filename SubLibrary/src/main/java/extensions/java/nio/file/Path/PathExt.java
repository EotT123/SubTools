package extensions.java.nio.file.Path;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.ExtensionSource;
import manifold.ext.rt.api.MethodSignature;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.sublibrary.util.CopyDirVisitor;
import org.lodder.subtools.sublibrary.util.DeleteDirVisitor;

@Extension
@ExtensionSource(source = Files.class, methods = {@MethodSignature(name = "size", paramTypes = {Path.class})})
public class PathExt {

    private PathExt() {
        // hide utility class constructor
    }

    public static String getExtension(@This Path path) {
        return StringUtils.substringAfterLast(path.getFileName().toString(), ".");
    }

    public static boolean hasExtension(@This Path path, String extension) {
        return extension.equalsIgnoreCase(getExtension(path));
    }

    public static String changeExtension(@This Path path, String newExtension) {
        return StringUtils.substringBeforeLast(path.getFileName().toString(), ".") + "." + newExtension;
    }

    public static String withoutExtension(@This Path path) {
        return changeExtension(path, "");
    }

    public record FilenameAndExtension(String filename, String extension) {
    }

    public static FilenameAndExtension splitExtension(@This Path path) {
        return new FilenameAndExtension(StringUtils.substringBeforeLast(path.getFileName().toString(), "."),
            StringUtils.substringAfterLast(path.getFileName().toString(), "."));
    }

    public static String withoutExtension(String path) {
        return StringUtils.substringBeforeLast(path, ".");
    }

    public static String toAbsolutePathAsString(@This Path path) {
        return path.toAbsolutePath().toString();
    }

    /**
     * Moves a file or a complete directory tree.
     * <p>
     * This method moves the given {@link Path} to the specified destination. Depending on whether the path is a
     * directory or a regular file, the behavior of the method is as follows:
     * <ul>
     * <li>If the {@link Path} is a directory, it will recursively move all files and subdirectories
     * within the directory hierarchy, starting from and including the given path, to the
     * destination.</li>
     * <li>If the {@link Path} is a regular file, it will be moved to the destination file.</li>
     * </ul>
     * <p>
     * The method also allows you to specify optional move options, such as whether to replace an
     * existing file or directory, using the {@link StandardCopyOption} enum. These options are
     * applied to all files and directories being moved.
     *
     * @param source the path to be moved
     * @param destinationDir the destination directory
     * @param copyOptions optional move options to apply while moving the path
     * @return the destination
     * @throws IOException if an I/O error occurs while moving the path
     */
    public static Path moveToDir(@This Path source, Path destinationDir, StandardCopyOption... copyOptions)
        throws IOException {
        return moveToDirAndRename(source, destinationDir, source.getFileName().toString(), copyOptions);
    }

    /**
     * Moves a file or a complete directory tree.
     * <p>
     * This method moves the given {@link Path} to the specified destination. Depending on whether the path is a
     * directory or a regular file, the behavior of the method is as follows:
     * <ul>
     * <li>If the {@link Path} is a directory, it will recursively move all files and subdirectories
     * within the directory hierarchy, starting from and including the given path, to the
     * destination.</li>
     * <li>If the {@link Path} is a regular file, it will be moved to the destination file.</li>
     * </ul>
     * <p>
     * The moved path is also renamed to the provided new name.
     * The method also allows you to specify optional move options, such as whether to replace an
     * existing file or directory, using the {@link StandardCopyOption} enum. These options are
     * applied to all files and directories being moved.
     *
     * @param source the path to be moved
     * @param destinationDir the destination directory
     * @param newFileName the new file name
     * @param copyOptions optional move options to apply while moving the path
     * @return the destination
     * @throws IOException if an I/O error occurs while moving the path
     */
    public static Path moveToDirAndRename(@This Path source, Path destinationDir, String newFileName,
        StandardCopyOption... copyOptions)
        throws IOException {
        Files.createDirectories(destinationDir);
        if (Files.isRegularFile(source)) {
            Files.move(source, destinationDir.resolve(newFileName), copyOptions);
        } else {
            try {
                Files.move(source, destinationDir.resolve(newFileName), copyOptions);
            } catch (DirectoryNotEmptyException e) {
                // happens when moving a non-empty folder to another drive
                moveNonEmptyDirectory(source, destinationDir, copyOptions);
            }
        }
        return destinationDir.resolve(newFileName);
    }

    private static Path moveNonEmptyDirectory(Path sourceDir, Path targetDir, StandardCopyOption... copyOptions)
        throws IOException {
        if (Files.isDirectory(sourceDir)) {
            return moveNonEmptyDirectoryRecursively(sourceDir, targetDir, copyOptions);
        } else {
            return moveToDir(sourceDir, targetDir, copyOptions);
        }
    }

    private static Path moveNonEmptyDirectoryRecursively(Path source, Path target, StandardCopyOption... copyOptions)
        throws IOException {
        foreachSubfile(source,
            s -> s.forEachEx(child -> moveNonEmptyDirectory(child, target.resolve(source.getFileName()), copyOptions)));
        Files.delete(source);
        return target;
    }

    /**
     * Deletes a {@link Path}.
     * <p>
     * If the {@link Path} exists, this method will delete it without any possibility of recovery. This method behaves
     * as follows:
     * <ul>
     * <li>If the {@link Path} is a directory, it will recursively delete all files and
     * subdirectories within the directory, starting from and including the given path.</li>
     * <li>If the {@link Path} is a regular file, it will be deleted.</li>
     * </ul>
     *
     * @param path the path to delete
     * @throws IOException if an I/O error occurs while deleting the path
     */
    // TODO change name? (nameclash)
    public static void deletePath(@This Path path) throws IOException {
        Files.walkFileTree(path, new DeleteDirVisitor());
    }

    /**
     * Copies a file or a complete directory tree.
     * <p>
     * This method copies the given {@link Path} to the specified destination. Depending on whether the path is a
     * directory or a regular file, the behavior of the method is as follows:
     * <ul>
     * <li>If the {@link Path} is a directory, it will recursively copy all files and subdirectories
     * within the directory hierarchy, starting from and including the given path, to the
     * destination.</li>
     * <li>If the {@link Path} is a regular file, it will be copied to the destination file.</li>
     * </ul>
     * <p>
     * The method also allows you to specify optional copy options, such as whether to replace an
     * existing file or directory, using the {@link StandardCopyOption} enum. These options are
     * applied to all files and directories being copied.
     *
     * @param source the path to be copied
     * @param destinationDir the destination directory
     * @param copyOptions optional copy options to apply while copying the path
     * @return the location of the copied path
     * @throws IOException if an I/O error occurs while deleting the path
     */
    public static Path copyToDir(@This Path source, Path destinationDir, StandardCopyOption... copyOptions)
        throws IOException {
        return copyToDirAndRename(source, destinationDir, source.getFileName().toString(), copyOptions);
    }

    /**
     * Copies a file or a complete directory tree.
     * <p>
     * This method copies the given {@link Path} to the specified destination. Depending on whether the path is a
     * directory or a regular file, the behavior of the method is as follows:
     * <ul>
     * <li>If the {@link Path} is a directory, it will recursively copy all files and subdirectories
     * within the directory hierarchy, starting from and including the given path, to the
     * destination.</li>
     * <li>If the {@link Path} is a regular file, it will be copied to the destination file.</li>
     * </ul>
     * <p>
     * The copied path is also renamed to the provided new name.
     * The method also allows you to specify optional copy options, such as whether to replace an
     * existing file or directory, using the {@link StandardCopyOption} enum. These options are
     * applied to all files and directories being copied.
     *
     * @param source the path to be copied
     * @param destinationDir the destination directory
     * @param newFileName the new file name
     * @param copyOptions optional copy options to apply while copying the path
     * @return the location of the copied path
     * @throws IOException if an I/O error occurs while deleting the path
     */
    public static Path copyToDirAndRename(@This Path source, Path destinationDir, String newFileName,
        StandardCopyOption... copyOptions)
        throws IOException {
        if (Files.isRegularFile(source)) {
            Files.createDirectories(destinationDir);
            Path destinationFile = destinationDir.resolve(newFileName);
            Files.copy(source, destinationFile, copyOptions);
            return destinationFile;
        } else {
            Path destination = destinationDir.resolve(newFileName);
            Files.createDirectories(destination);
            Files.walkFileTree(source, new CopyDirVisitor(source, destination, copyOptions));
            return destination;
        }
    }

    public static String getFileNameAsString(@This Path path) {
        return path.getFileName().toString();
    }

    public static boolean fileNameContains(@This Path path, String text) {
        return path.getFileName().toString().contains(text);
    }

    public static boolean fileNameContainsIgnoreCase(@This Path path, String text) {
        return StringUtils.containsIgnoreCase(path.getFileName().toString(), text);
    }

    public static boolean isEmptyDir(@This Path path) throws IOException {
        requireDir(path);
        return applySubfiles(path, children -> children.findAny().isEmpty());
    }

    public static <T, X extends Exception> T applySubfiles(@This Path path,
        ThrowingFunction<Stream<Path>, T, X> function) throws IOException, X {
        try (Stream<Path> pathStream = Files.list(path)) {
            return function.apply(pathStream);
        }
    }

    public static <X extends Exception> void foreachSubfile(@This Path path, ThrowingConsumer<Stream<Path>, X> consumer)
        throws IOException, X {
        try (Stream<Path> pathStream = Files.list(path)) {
            consumer.accept(pathStream);
        }
    }

    public static void requireDir(@This Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("[%s] is not a directory".formatted(path));
        }
    }

    /////////////////////

    public static void unzip(InputStream inputStream, Path outputDir, String extensionFilter) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (!ze.isDirectory() && ze.getName().endsWith(extensionFilter)) {
                    Path outputPath = outputDir.resolve(ze.getName()).normalize();

                    // Prevent Zip Slip
                    if (!outputPath.startsWith(outputDir)) {
                        throw new IOException("Bad zip entry: " + ze.getName());
                    }

                    Files.createDirectories(outputPath.getParent());
                    try (OutputStream out = Files.newOutputStream(outputPath)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    public static boolean isZipFile(InputStream inputStream) throws IOException {
        InputStream is = inputStream;
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(4);

        byte[] header = new byte[4];
        int bytesRead = is.read(header);
        is.reset();

        if (bytesRead < 4) {
            return false; // not enough bytes to be a zip file
        }

        int magic = ((header[0] & 0xFF)) |
            ((header[1] & 0xFF) << 8) |
            ((header[2] & 0xFF) << 16) |
            ((header[3] & 0xFF) << 24);

        return magic == 0x504b0304; // ZIP magic number (little-endian)
    }

    /*
     * Determines if a byte array is compressed. The java.util.zip GZip
     * implementation does not expose the GZip header, so it is difficult to
     * determine if a string is compressed.
     *
     * @param bytes an array of bytes
     *
     * @return true if the array is compressed or false otherwise
     *
     * @throws java.io.IOException if the byte array couldn't be read
     */
    public static byte[] decompressGZip(byte[] data) throws IOException {
        try (ByteArrayInputStream binput = new ByteArrayInputStream(data);
             GZIPInputStream gzinput = new GZIPInputStream(binput)) {
            return gzinput.readAllBytes();
        }
    }

    public static boolean isGZipCompressed(byte[] data) {
        return data.length >= 2 && data[0] == (byte) GZIPInputStream.GZIP_MAGIC &&
            data[1] == (byte) GZIPInputStream.GZIP_MAGIC >> 8;
    }
}
