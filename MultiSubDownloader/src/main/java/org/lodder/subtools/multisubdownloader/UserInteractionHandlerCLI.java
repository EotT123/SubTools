package org.lodder.subtools.multisubdownloader;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static org.lodder.subtools.multisubdownloader.gui.extra.table.SubtitleTableColumnName.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import extensions.org.codehaus.plexus.components.interactivity.Prompter.PrompterExt;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.extra.table.SubtitleTableColumnName;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class UserInteractionHandlerCLI extends org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandlerCLI
    implements UserInteractionHandler {

    public UserInteractionHandlerCLI(UserInteractionSettingsIntf settings) {
        super(settings);
    }

    @Override
    public List<Subtitle> selectSubtitles(Release release) {
        System.out.printf("\n%s : %s%n", getText("SelectDialog.SelectCorrectSubtitleThisRelease"),
            release.fileNameOrName);
        return PrompterExt.promptValuesFromList(prompter,
            getText("SelectDialog.EnterListSelectedSubtitles"),
            release.matchingSubs,
            Subtitle::getFileName,
            true,
            createTableDisplayer(),
            Comparator.comparing(Subtitle::getScore));
    }

    private PrompterExt.ColumnDisplayer<Subtitle> createSubtitleDisplayer(SubtitleTableColumnName column,
        Function<Subtitle, Object> toStringMapper) {
        return new PrompterExt.ColumnDisplayer<>(column.columnName,
            subtitle -> String.valueOf(toStringMapper.apply(subtitle)));
    }

    @Override
    public void dryRunOutput(Release release) {
        createTableDisplayer().display(release.matchingSubs);
    }

    private PrompterExt.TableDisplayer<Subtitle> createTableDisplayer() {
        return new PrompterExt.TableDisplayer<>(
            Stream.of(SCORE, FILENAME, RELEASEGROUP, QUALITY, SOURCE, UPLOADER, HEARINGIMPAIRED)
                .map(stcn -> createSubtitleDisplayer(stcn, stcn.valueFunction)).toList());
    }
}
