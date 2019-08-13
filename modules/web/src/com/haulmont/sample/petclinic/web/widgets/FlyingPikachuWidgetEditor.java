package com.haulmont.sample.petclinic.web.widgets;

import com.haulmont.addon.dashboard.model.Parameter;
import com.haulmont.addon.dashboard.model.Widget;
import com.haulmont.addon.dashboard.model.paramtypes.ParameterValue;
import com.haulmont.addon.dashboard.model.paramtypes.StringParameterValue;
import com.haulmont.addon.dashboard.web.annotation.WidgetParam;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.HasValue;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.AbstractDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.model.DataContext;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.gui.screen.ScreenFragment;
import com.haulmont.cuba.gui.screen.StandardEditor;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.Target;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("petclinic_FlyingPikachuWidgetEditor")
@UiDescriptor("flying-pikachu-widget-editor.xml")
public class FlyingPikachuWidgetEditor extends ScreenFragment {
    @Inject
    private LookupField artLookup;
    @Inject
    private Logger log;

    @WindowParam
    @WidgetParam
    protected String petName;

    @WindowParam
    @WidgetParam
    protected String petArtResource;

    private final static String ART_RESOURCES_PATH = "../webapps/petclinic/VAADIN/images";
    private final static String ART_SUFFIX = "_art.png";
    private final static String RESOURCES_ROOT_PATH = "VAADIN/images/";
    private final static String DEFAULT_ART = "pikachu_art.png";

    private HashMap<String, String> resourcesMap = new HashMap<>();

    @Subscribe
    private void onInit(InitEvent event) {
        List<String> files = listArtFiles(ART_RESOURCES_PATH);
        for (String artFile : files) {
            String pokemonName = artFile.replace(ART_SUFFIX, "").toLowerCase();
            pokemonName = pokemonName.substring(0, 1).toUpperCase() + pokemonName.substring(1);
            String resource = RESOURCES_ROOT_PATH + artFile;
            resourcesMap.put(pokemonName, resource);
        }

        List<String> optionsList = new ArrayList<>(resourcesMap.keySet());
        optionsList.sort(Collator.getInstance());
        artLookup.setOptionsList(optionsList);
    }

    @Subscribe("artLookup")
    private void onArtLookupValueChange(HasValue.ValueChangeEvent event) {
        String selectedName = (String) event.getValue();
        if (selectedName != null) {
            petName = selectedName;
            petArtResource = resourcesMap.getOrDefault(selectedName, DEFAULT_ART);
        }
    }

    private List<String> listArtFiles(String path) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(fName -> fName.endsWith(ART_SUFFIX))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Can't read list of pokemon arts from " + path, e);
            return new ArrayList<>();
        }
    }
}