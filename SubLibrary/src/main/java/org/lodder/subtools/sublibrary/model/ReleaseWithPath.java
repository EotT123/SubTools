package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public sealed interface ReleaseWithPath extends Release permits MovieReleaseWithPath, TvReleaseWithPath {

    @val Path path;

    @val String fileName = path.getFileNameAsString();

    @val String releaseDescription = fileName;

    default boolean hasExtension(String extension) {
        return fileName.endsWith(extension);
    }
}
