/**
 * @file StageManager.java
 * @version 0.1.0-dev
 * 
 * @attention This version of the StageManager was written to fully support JavaFX version 17.0.2.
 * 
 * @todo Apply Optional fields
 * @todo page tree for navigation
 * @todo Multi-stage management
 * @todo Stage binding, the JavaBeans way
 * @todo Multi-screen management
 * @todo Multi-cursor/touch dragging controller
 */

 package ku.cs.oakcoding.app.services.stages;

 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.NotDirectoryException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardOpenOption;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Objects;
 import java.util.Optional;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javafx.application.Application;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.event.EventHandler;
 import javafx.event.EventTarget;
 import javafx.event.EventType;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.geometry.Rectangle2D;
 import javafx.scene.Group;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.MenuBar;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.input.PickResult;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Background;
 import javafx.scene.layout.BackgroundFill;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.Priority;
 import javafx.scene.layout.Region;
 import javafx.scene.layout.StackPane;
 import javafx.scene.paint.Color;
 import javafx.scene.robot.Robot;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.Text;
 import javafx.stage.Modality;
 import javafx.stage.Screen;
 import javafx.stage.Stage;
 import javafx.stage.StageStyle;
 import javafx.stage.WindowEvent;
 import ku.cs.oakcoding.app.helpers.configurations.OakAppConfigs;
 import ku.cs.oakcoding.app.helpers.configurations.OakAppDefaults;
 import ku.cs.oakcoding.app.helpers.configurations.OakSystemDefaults;
 import ku.cs.oakcoding.app.helpers.file.OakResourcePrefix;
 
 /**
  * Manager for the primary application Stage
  * 
  * @attention This version of the StageManager was written to fully support
  *            JavaFX version 17.0.2.
  * 
  * @note This version of the StageManager only supports single-stage application
  * 
  * @brief This class is following the singleton pattern since there
  *        MUST only be one instance of the StageManager that takes
  *        control over the primary @link javafx.stage.Stage @unlink
  *        provided by the platform.
  * 
  *        Controller MUST be declared in either FXML resource file or in
  *        FXML index property file, not both.
  *        Lines starting with @ref COMMENT_SYMBOL will be ignored.
  * 
  * 
  * The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
  * "SHOULD NOT", "RECOMMENDED",  "MAY", and "OPTIONAL" in this document are
  * to be interpreted as described in RFC 2119 <https://www.ietf.org/rfc/rfc2119.txt>.
  * 
  * 
  * @section Synopsis
  * 
  * \begin{enumerate}
  *      \item{Bind the primary application Stage with the StageManager's Stage}
  *      \item{Define the home page for your application}
  *      \item{Show your application Stage with page set to home}
  * \end{enumerate}
  * 
  * 
  * @section Overall References
  * 
  * References that are more specific to some sections were directly added to
  * the section heading.
  * 
  * - Event Processing
  *   https://docs.oracle.com/javafx/2/events/processing.htm
  */
 public final class StageManager {
 
     /**
      * Logger instance for this StageManager
      */
     private Logger logger;
 
     /**
      * Use custom Logger instead of StageManager's built-in
      */
     public void setLogger(Logger logger) {
         this.logger = logger;
     }
 
     /**
      * If required, controller to be used MUST be written in singleton pattern
      * 
      * Name of the method for getting controller type instance
      */
     private static final String GET_INSTANCE_METHOD_NAME;
 
     /**
      * Lines in FXML index property file begining with this charactor are comments and will be ignored
      */
     public static final char COMMENT_SYMBOL;
 
     /**
      * Default width of the Stage
      */
     private static final double DEFAULT_STAGE_WIDTH;
 
     /**
      * Default height of the Stage
      */
     private static final double DEFAULT_STAGE_HEIGHT;
 
     private static StageManager instance;
 
     /**
      * Configuration file for this StageManager
      */
     private static Optional<Properties> stageManagerConfigProperties;
 
     /**
      * Special nickName pool
      */
     private SpecialNick specialNickPool;
 
     /**
      * Path to the FXML index property file
      */
     private Path fxmlResourceIndexPath;
 
     /**
      * Prefix Path of FXML files to be loaded
      */
     private Path fxmlResourcePrefixPath;
 
     /**
      * Table storing all of the page entries each maps to its corresponding ParentMap
      * 
      * @see PageMap
      */
     private Map<String, PageMap> pageTable;
 
     /**
      * Properties type storing all the records loaded from the FXML index property file
      */
     private Properties resourceIndexProperties;
 
     /**
      * The instance of the main pageNick point of the JavaFX application
      */
     private Object mainApp;
 
     /**
      * An object to keep throughout Page transitions
      */
     private Object context;
 
     /**
      * The primary Stage of the application
      */
     private Stage primaryStage;
 
     /**
      * If true, Stages constructed by this StageManager will always positioned at the center of current Screen.
      * Otherwise, Stage will never be re-positioned.
      * Defaults to true.
      */
     private boolean alwaysCenteredStage;
 
     /**
      * StageStyle of the primary Stage
      */
     private StageStyle primaryStageStyle;
 
     /**
      * Title of the primary Stage
      */
     private String primaryStageTitle;
 
     /**
      * Menu bar of the primary Stage
      */
     private MenuBar primaryStageMenuBar;
 
     /**
      * Root page of the primary Stage
      */
     private Scene primaryStageScene;
 
     /**
      * Root page/Node/Parent of the primary Stage
      */
     private AnchorPane primaryStageScenePage;
 
     /**
      * Current root page/Node/Parent of the primary Stage
      */
     private Parent currentPrimaryStageScenePage;
 
     private ObjectProperty<String> currentPrimaryStageScenePageNick = new SimpleObjectProperty<>("DNE");
 
     private StackPane currentPrimaryStageScenePageContent;
 
     /**
      * Width of the primary Stage
      * 
      * @see DEFAULT_STAGE_WIDTH
      */
     private double primaryStageWidth;
 
     /**
      * Height of the primary Stage
      * 
      * @see DEFAULT_STAGE_HEIGHT
      */
     private double primaryStageHeight;
 
     /**
      * Page pageNick of the primary page with non-whitescape characters
      */
     private String homePageNick;
 
     /**
      * The annotation used to annotate types, states, or behaviours required by this StageManager
      */
     @Retention (RetentionPolicy.RUNTIME)
     @Target ({ ElementType.TYPE, ElementType.METHOD })
     public @interface TheStageManager {}
 
     /**
      * The record type for storing a pair of page and, if required, its corresponding controller
      */
     private record PageMap(Parent parent,
                            Class<?> controllerClass,
                            Optional<Double> prefWidth,
                            Optional<Double> prefHeight,
                            Boolean inheritWidth,
                            Boolean inheritHeight) {}
 
     /**
      * Attribute name in an FXML parent
      */
     private enum FXMLAttribute {
 
         PANE_PREF_HEIGHT     ("prefHeight"),
         PANE_PREF_WIDTH      ("prefWidth");
 
         private final String value;
 
         private FXMLAttribute(String value) {
             this.value = value;
         }
     }
 
     /**
      * Thrown to indicate that the code has attempted to manipulate a page that is not in the page table
      * 
      * @see pageTable
      * @see addPage
      * @see removePage
      */
     public final class PageNotFoundException
             extends Exception {
 
         public PageNotFoundException() {}
 
         public PageNotFoundException(String pageNick) {
             super(pageNick);
         }
 
         public PageNotFoundException(Throwable cause) {
             super(cause);
         }
 
         public PageNotFoundException(String pageNick,
                                        Throwable cause) {
 
             super(pageNick, cause);
         }
 
         public PageNotFoundException(String message,
                                        Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
 
             super(message, cause, enableSuppression, writableStackTrace);
         }
     }
 
     /**
      * Thrown to indicate that the FXML index property file being read is malformed
      */
     public final class MalformedFXMLIndexFileException
             extends Exception {
 
         public MalformedFXMLIndexFileException() {}
 
         public MalformedFXMLIndexFileException(String message) {
             super(message);
         }
 
         public MalformedFXMLIndexFileException(Throwable cause) {
             super(cause);
         }
 
         public MalformedFXMLIndexFileException(String message,
                                                Throwable cause) {
 
             super(message, cause);
         }
     }
 
     /**
      * Thrown to indicate that there is no controller declared for a certain page
      */
     public final class NoControllerSpecifiedException
             extends Exception {
 
         public NoControllerSpecifiedException() {}
 
         public NoControllerSpecifiedException(String message) {
             super(message);
         }
 
         public NoControllerSpecifiedException(Throwable cause) {
             super(cause);
         }
 
         public NoControllerSpecifiedException(String message,
                                               Throwable cause) {
 
             super(message, cause);
         }
     }
 
     /**
      * Object holding parsed record information of a certain page
      */
     private class ParentProperty {
 
         private String pageNick;
 
         private String pageNickResourceName;
 
         private Boolean inheritWidth;
 
         private Boolean inheritHeight;
 
         private Optional<String> controllerClassName;
 
         public ParentProperty(String pageNick,
                               String pageNickResourceName,
                               Boolean inheritWidth,
                               Boolean inheritHeight,
                               Optional<String> controllerClassName) {
 
             this.pageNick = pageNick;
             this.pageNickResourceName = pageNickResourceName;
             this.inheritWidth = inheritWidth;
             this.inheritHeight = inheritHeight;
             this.controllerClassName = controllerClassName;
         }
 
         public String pageNick() {
             return pageNick;
         }
 
         public String pageNickResourceName() {
             return pageNickResourceName;
         }
 
         public Boolean getInheritWidth() {
             return inheritWidth;
         }
 
         public Boolean getInheritHeight() {
             return inheritHeight;
         }
 
         public Optional<String> getControllerClassName() {
             return controllerClassName;
         }
     }
 
     /**
      * Parse property value from FXML index file
      * 
      * <pageNick> "=" <fxml_resource_name.fxml> "|" <fixed_width "W" | inherit_width "w"> "," <fixed_height "H" | inherit_height "h"> "|" [ "=>" <controller> ]
      * 
      * fixed_width, fixed_height:        construct a stage using width/height defined in FXML resource file (.fxml)
      * inherit_width, inherit_height:    construct a stage using inherited width/height from its the previous page set to Stage
      * 
      * e.g.,
      *  home=home.fxml|w,H|=>com.naiithink.app.controllers.HomeController
      * 
      * @note value MUST be written in order from left to right
      */
     private static class Utils {
 
         /**
          * FXML index property parsing patterns
          */
         private enum IndexPropertyRegex {
 
             WIDTH_INHERIT                           (null,                   "w"),
             HEIGHT_INHERIT                          (null,                   "h"),
             VALIDATE                                (new int[] { 0 },        "(^\\w+)(?:=\\s*)(\\w+\\.fxml)(?:\\s*\\|\\s*)([WwHh])(?:\\s*,\\s*)([WwHh])(?:\\s*\\|\\s*)(?:(?:\\s*=>\\s*)([\\w\\.]+))?"),
             VALIDATE_NO_KEY                         (new int[] { 0 },        "(?<=\\W)*(\\w+\\.fxml)(?:\\s*\\|\\s*)([WwHh])(?:\\s*,\\s*)([WwHh])(?:\\s*\\|\\s*)(?:(?:\\s*=>\\s*)([\\w\\.]+))?"),
             VALIDATE_NO_KEY_FXML_RESOURCE_NAME      (new int[] { 1 },        null),
             VALIDATE_NO_KEY_WIDTH_INHERITANCE       (new int[] { 2 },        null),
             VALIDATE_NO_KEY_HEIGHT_INHERITANCE      (new int[] { 3 },        null),
             VALIDATE_NO_KEY_CONTROLLER_NAME         (new int[] { 4 },        null),
             VALIDATE_PAGE_NICKNAME                   (new int[] { 1 },       null),
             VALIDATE_FXML_RESOURCE_NAME             (new int[] { 2 },        null),
             VALIDATE_WIDTH_INHERITANCE              (new int[] { 3 },        null),
             VALIDATE_HEIGHT_INHERITANCE             (new int[] { 4 },        null),
             VALIDATE_CONTROLLER_NAME                (new int[] { 5 },        null),
             ROOT_DILIMITER                          (new int[] { -1 },       "=" ),
             PAGE_NICKNAME                           (new int[] { 0 },        "\\w+(?=\\s*=)"),
             FXML_RESOURCE_NAME                      (new int[] { 0 },        "(?<=\\W)*\\w+\\.(fxml)(?=\\W+)"),
             DIMENSIONAL_INHERITANCE                 (new int[] { 1, 2 },     "(?<=\\|\\s*)([wWhH])(?:\\s*,\\s*)([wWhH])(?=\\s*\\|)"),
             CONTROLLER_NAME                         (new int[] { 0 },        "(?<==>\\s*).*");
 
             private final int[] groupToGet;
 
             private final String pattern;
 
             private IndexPropertyRegex(int[] groupToGet, String pattern) {
                 this.groupToGet = groupToGet;
                 this.pattern = pattern;
             }
         }
 
         public static ParentProperty readProperty(boolean keyExist,
                                                   String propertyValue) throws MalformedFXMLIndexFileException {
 
             String pageNick;
             String fxmlResourceName;
             Boolean inheritWidth;
             Boolean inheritHeight;
             Optional<String> controllerClassName;
 
             Pattern p;
 
             if (keyExist) {
                 p = Pattern.compile(IndexPropertyRegex.VALIDATE.pattern);
             } else {
                 p = Pattern.compile(IndexPropertyRegex.VALIDATE_NO_KEY.pattern);
             }
 
             Matcher m = p.matcher(propertyValue);
 
             if (m.find() == false) {
                 throw instance.new MalformedFXMLIndexFileException();
             }
 
             int groupCount = m.groupCount();
             String[] result = new String[groupCount+1];
 
             for (int i = 1, lim = groupCount + 1; i < lim; ++i) {
                 result[i] = m.group(i);
             }
 
             if (keyExist) {
                 pageNick = result[IndexPropertyRegex.VALIDATE_PAGE_NICKNAME.groupToGet[0]].trim();
                 fxmlResourceName = result[IndexPropertyRegex.VALIDATE_FXML_RESOURCE_NAME.groupToGet[0]].trim();
                 inheritWidth = result[IndexPropertyRegex.VALIDATE_WIDTH_INHERITANCE.groupToGet[0]].trim().equals(IndexPropertyRegex.WIDTH_INHERIT.pattern);
                 inheritHeight = result[IndexPropertyRegex.VALIDATE_HEIGHT_INHERITANCE.groupToGet[0]].trim().equals(IndexPropertyRegex.HEIGHT_INHERIT.pattern);
                 controllerClassName = Optional.ofNullable(result[IndexPropertyRegex.VALIDATE_CONTROLLER_NAME.groupToGet[0]]);
             } else {
                 pageNick = null;
                 fxmlResourceName = result[IndexPropertyRegex.VALIDATE_NO_KEY_FXML_RESOURCE_NAME.groupToGet[0]].trim();
                 inheritWidth = result[IndexPropertyRegex.VALIDATE_NO_KEY_WIDTH_INHERITANCE.groupToGet[0]].trim().equals(IndexPropertyRegex.WIDTH_INHERIT.pattern);
                 inheritHeight = result[IndexPropertyRegex.VALIDATE_NO_KEY_HEIGHT_INHERITANCE.groupToGet[0]].trim().equals(IndexPropertyRegex.HEIGHT_INHERIT.pattern);
                 controllerClassName = Optional.ofNullable(result[IndexPropertyRegex.VALIDATE_NO_KEY_CONTROLLER_NAME.groupToGet[0]]);
             }
 
             if (controllerClassName.isPresent()) {
                 controllerClassName = controllerClassName.map(s -> {
                     return s.trim();
                 });
             }
 
             return instance.new ParentProperty(pageNick, fxmlResourceName, inheritWidth, inheritHeight, controllerClassName);
         }
     }
 
     /**
      * Special nickName parser
      */
     private class SpecialNick {
 
         /**
          * Lines in FXML index property file begining with this charactor are special nickNames
          */
         public static final char SPECIAL_NICK_SYMBOL;
 
         public enum SpecialNicks {
 
             CUSTOM_TITLE_BAR        ("title_bar");
 
             private final String nick;
 
             private SpecialNicks(String nick) {
                 this.nick = nick;
             }
         }
 
         /**
          * Map of special nickName to its corresponding value
          */
         private Map<String, Optional<Object>> specialNickMap;
 
         static {
             SPECIAL_NICK_SYMBOL = '$';
         }
 
         protected SpecialNick() {
             specialNickMap = new ConcurrentHashMap<>();
         }
 
         /**
          * Adds special nickName with its mapped values
          * 
          * @param       nickName        Special nickName
          * @param       value           Value mapped to by nickName
          */
         void putRecord(String nickName,
                               Optional<Object> value) {
 
             specialNickMap.put(nickName, value);
         }
 
         /**
          * Gets a special nickName object
          * 
          * @param       nickName        Special nickName
          */
         Optional<Object> getRecord(String nickName) {
             if (specialNickMap.containsKey(nickName) == false) {
                 logger.log(Level.SEVERE, "Special nickName map does not contain nick: '" + nickName + "'");
 
                 return null;
             }
 
             return specialNickMap.get(nickName);
         }
 
         boolean containsNick(String nickName) {
             return specialNickMap.containsKey(nickName);
         }
 
         /**
          * Gets the unmodifiable special nickName with its mapped values
          */
         public Map<String, Object> getSpecialNickMap() {
             return Collections.unmodifiableMap(specialNickMap);
         }
     }
 
     static {
         GET_INSTANCE_METHOD_NAME = "getInstance";
         COMMENT_SYMBOL = '#';
         DEFAULT_STAGE_WIDTH = 800.0;
         DEFAULT_STAGE_HEIGHT = 600.0;
         DEFAULT_STAGE_TITLE_BAR_HEIGHT = 30.0;
     }
 
     /**
      * Contructor is kept private, singleton
      */
     private StageManager() {
         logger = Logger.getLogger(getClass().getName());
 
         specialNickPool = new SpecialNick();
 
         alwaysCenteredStage = true;
         pageTable = new ConcurrentHashMap<>();
         resourceIndexProperties = new Properties();
 
         constructDev();
 
         primaryStage = null;
     }
 
     private void constructDev() {
         initializeSectionFont();
         initializeSectionDraggability();
         initializeSectionCustomStage();
     }
 
     /**
      * Gets the instance of StageManager
      */
     public static StageManager getStageManager() {
         if (instance == null) {
             synchronized (StageManager.class) {
                 if (instance == null) {
                     instance = new StageManager();
                 }
             }
         }
 
         return instance;
     }
 
     public Stage getPrimaryStage() {
         return this.primaryStage;
     }
 
     public ObjectProperty<String> getCurrentPrimaryStageScenePageNickProperty() {
         return this.currentPrimaryStageScenePageNick;
     }
 
     public boolean checkCurrentPage(String pageNick) {
         if (this.currentPrimaryStageScenePageNick.isNull().get())
             return false;
         else
             return this.currentPrimaryStageScenePageNick.get().equals(pageNick);
     }
 
     /**
      * Binds the application Stage with StageManager's Stage
      * 
      * @param       fxmlResourceIndexPath       Path to the FXML index property file
      * @param       fxmlResourcePrefixPath      Prefix Path of FXML files to be loaded
      * @param       mainApp                     The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                The primary Stage of the application
      * @param       primaryStageStyle           Style of the primary Stage
      * @param       primaryStageTitle           Title of the primary Stage
      * @param       primaryStageWidth           Width of the primary Stage
      * @param       primaryStageHeight          Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(Path fxmlResourceIndexPath,
                           Path fxmlResourcePrefixPath,
                           Object mainApp,
                           Stage primaryStage,
                           StageStyle primaryStageStyle,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         Objects.nonNull(fxmlResourceIndexPath);
         Objects.nonNull(mainApp);
         Objects.nonNull(primaryStage);
 
         if (Application.class.isAssignableFrom(mainApp.getClass()) == false) {
             logger.log(Level.SEVERE, "Unable to take control over main JavaFX pageNick, invalid argument type for parameter 'mainApp'");
 
             System.exit(1);
         }
 
         this.fxmlResourceIndexPath = fxmlResourceIndexPath;
         this.fxmlResourcePrefixPath = fxmlResourcePrefixPath;
         this.mainApp = mainApp;
         this.primaryStage = primaryStage;
         this.primaryStageTitle = primaryStageTitle;
         this.primaryStageWidth = primaryStageWidth;
         this.primaryStageHeight = primaryStageHeight;
         
         if (primaryStageStyle == null) {
             this.primaryStageStyle = StageStyle.DECORATED;
         } else {
             this.primaryStageStyle = primaryStageStyle;
         }
 
         try (InputStream in = Files.newInputStream(fxmlResourceIndexPath)) {
 
             Objects.nonNull(in);
 
             resourceIndexProperties.load(in);
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Cannot get to FXML index file");
         }
 
         Optional<String> controllerClassName;
         Class<?> controllerClass;
         Method getInstanceMethod;
         Object controllerInstance;
 
         Optional<Double> prefWidth;
         Optional<Double> prefHeight;
 
         FXMLLoader loader;
         Parent parent;
 
         try {
             Set<String> parentEntries = resourceIndexProperties.stringPropertyNames();
 
             for (String pageNick : parentEntries) {
                 // Special symbol record
                 if (pageNick.charAt(0) == SpecialNick.SPECIAL_NICK_SYMBOL) {
 
                     if (resourceIndexProperties.get(pageNick) == null) {
                         logger.log(Level.SEVERE, "A special nickName record requires exactly two fields");
 
                         throw new MalformedFXMLIndexFileException();
                     }
 
                     this.specialNickPool.putRecord(pageNick.substring(1).trim(), Optional.ofNullable(resourceIndexProperties.get(pageNick).toString().trim()));
                     logger.log(Level.INFO, "Special nickName added: '" + pageNick.substring(1) + "': '" + resourceIndexProperties.get(pageNick) + "'");
 
                     continue;
                 }
 
                 loader = new FXMLLoader();
                 controllerClassName = Optional.empty();
 
                 prefWidth = Optional.empty();
                 prefHeight = Optional.empty();
 
                 ParentProperty parentProperty = Utils.readProperty(false, resourceIndexProperties.getProperty(pageNick));
 
                 prefWidth = Optional.of(
                     Double.valueOf(
                         getRootXMLAttribute(fxmlResourcePrefixPath.resolve(parentProperty.pageNickResourceName),
                                             null,
                                             FXMLAttribute.PANE_PREF_WIDTH.value).orElse("0.0")
                     )
                 );
 
                 prefHeight = Optional.of(
                     Double.valueOf(
                         getRootXMLAttribute(fxmlResourcePrefixPath.resolve(parentProperty.pageNickResourceName),
                                             null,
                                             FXMLAttribute.PANE_PREF_HEIGHT.value).orElse("0.0")
                     )
                 );
 
                 // Controller declared in FXML
                 if (parentProperty.controllerClassName.isPresent() == false) {
                     controllerClassName = Optional.ofNullable(getRootXMLAttribute(fxmlResourcePrefixPath.resolve(parentProperty.pageNickResourceName),
                                                               FXMLLoader.FX_NAMESPACE_PREFIX,
                                                               FXMLLoader.FX_CONTROLLER_ATTRIBUTE).orElseThrow(NoControllerSpecifiedException::new)
                     );
   
                     logger.log(Level.INFO, "Using controller declared in FXML resource file: '" + parentProperty.pageNickResourceName + "' for page pageNick: '" + pageNick + "'");
 
                     Pane page = FXMLLoader.load(fxmlResourcePrefixPath.resolve(parentProperty.pageNickResourceName).toUri().toURL());
 
                     if (this.primaryStageMenuBar != null) {
                         page.getChildren().add(this.primaryStageMenuBar);
                     }
 
                     pageTable.put(pageNick, new PageMap(page,
                                                         null,
                                                         prefWidth,
                                                         prefHeight,
                                                         parentProperty.inheritWidth,
                                                         parentProperty.inheritHeight));
 
                     StackPane.setAlignment(pageTable.get(pageNick).parent, Pos.CENTER);
 
                     logger.log(Level.INFO, "Page added: '" + pageNick + "' ::= '**/" + parentProperty.pageNickResourceName + "' -> '" + controllerClassName.get() + "'"); 
 
                     continue;
 
                 // Controller declared in resource index property file
                 } else {
                     controllerClassName = Optional.ofNullable(parentProperty.controllerClassName)
                                                   .orElseThrow(MalformedFXMLIndexFileException::new);
 
                     logger.log(Level.INFO, "Using controller declared in FXML index property file: '" + fxmlResourceIndexPath.getFileName().toString() + "' for page pageNick: '" + pageNick + "'");
                 }
 
                 controllerClass = Class.forName(controllerClassName.get());
 
                 if (controllerClass.isAnnotationPresent(StageManager.TheStageManager.class) == false) {
                     /**
                      * @danger DO NOT pass any message read from file to any string formatter
                      */
                     String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller of file '" + parentProperty.pageNickResourceName + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                      + "FXML controller MUST be annotated with '@" + StageManager.TheStageManager.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageManager.TheStageManager.class.getCanonicalName() + "' which returns the singleton instance of the controller itself";
 
                     logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);
 
                     System.exit(1);
                 }
 
                 getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);
 
                 if (getInstanceMethod.isAnnotationPresent(StageManager.TheStageManager.class) == false) {
                     logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");
 
                     System.exit(1);
                 }
 
                 controllerInstance = getInstanceMethod.invoke(null);
 
                 loader.setLocation(fxmlResourcePrefixPath.resolve(parentProperty.pageNickResourceName).toUri().toURL());
                 loader.setController(controllerInstance);
                 parent = loader.load();
 
                 pageTable.put(pageNick, new PageMap(parent,
                                                     controllerClass,
                                                     prefWidth,
                                                     prefHeight,
                                                     parentProperty.inheritWidth,
                                                     parentProperty.inheritHeight));
 
                 logger.log(Level.INFO, "Page added: '" + pageNick + "' ::= '**/" + parentProperty.pageNickResourceName + "' => '" + controllerClassName.get() + "'");
             }
         } catch (IOException e) {
             e.printStackTrace();
             logger.log(Level.SEVERE, "Got 'IOException' while loading FXML resource: " + e.getMessage());
         } catch (ClassNotFoundException e) {
             logger.log(Level.SEVERE, "Controller not found: " + e.getMessage());
         } catch (NoSuchMethodException e) {
             logger.log(Level.SEVERE, "Method '" + GET_INSTANCE_METHOD_NAME + "' not found");
         } catch (IllegalAccessException e) {
             /**
              * An IllegalAccessException is thrown when an application tries to reflectively create an instance (other than an array),
              * set or get a field, or invoke a method, but the currently executing method does not have access to the definition of
              * the specified class, field, method or constructor.
              */
             logger.log(Level.SEVERE, "Got illegal access: " + e.getMessage());
         } catch (InvocationTargetException e) {
             /**
              * InvocationTargetException is a checked exception that wraps an exception thrown by an invoked method or constructor.
              */
             logger.log(Level.SEVERE, "Got an exception while getting instance of controller");
         }
     }
 
     /**
      * Binds the application Stage with StageManager's Stage without customizing StageStyle
      * 
      * @param       fxmlResourceIndexPath       Path to the FXML index property file
      * @param       fxmlResourcePrefixPath      Prefix Path of FXML files to be loaded
      * @param       mainApp                     The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                The primary Stage of the application
      * @param       primaryStageTitle           Title of the primary Stage
      * @param       primaryStageWidth           Width of the primary Stage
      * @param       primaryStageHeight          Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(Path fxmlResourceIndexPath,
                           Path fxmlResourcePrefixPath,
                           Object mainApp,
                           Stage primaryStage,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         bindStage(fxmlResourceIndexPath,
                   fxmlResourcePrefixPath,
                   mainApp,
                   primaryStage,
                   null,
                   primaryStageTitle,
                   primaryStageWidth,
                   primaryStageHeight);
     }
 
     /**
      * Binds the application Stage with StageManager's Stage with an option to toggle the use of
      * StageStyle.TRANSPARENT for the primary Stage
      * 
      * @param       fxmlResourceIndexPath       Path to the FXML index property file
      * @param       fxmlResourcePrefixPath      Prefix Path of FXML files to be loaded
      * @param       mainApp                     The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                The primary Stage of the application
      * @param       transparentStageStyle       If true, use StageStyle.TRANSPARENT for the primary Stage. Otherwise, use StageStyle.UNDECORATED
      * @param       primaryStageTitle           Title of the primary Stage
      * @param       primaryStageWidth           Width of the primary Stage
      * @param       primaryStageHeight          Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(Path fxmlResourceIndexPath,
                           Path fxmlResourcePrefixPath,
                           Object mainApp,
                           Stage primaryStage,
                           boolean transparentStageStyle,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         StageStyle stageStyle;
 
         if (transparentStageStyle == true) {
             stageStyle = StageStyle.TRANSPARENT;
         } else {
             stageStyle = StageStyle.UNDECORATED;
         }
 
         bindStage(fxmlResourceIndexPath,
                   fxmlResourcePrefixPath,
                   mainApp,
                   primaryStage,
                   stageStyle,
                   primaryStageTitle,
                   primaryStageWidth,
                   primaryStageHeight);
     }
 
     /**
      * Binds the application Stage with StageManager's Stage
      * 
      * @param       fxmlResourceIndexPathString         String representation of the Path to the FXML index property file
      * @param       fxmlResourcePrefixPathString        String representation of the prefix Path of FXML files to be loaded
      * @param       mainApp                             The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                        The primary Stage of the application
      * @param       primaryStageStyle                   Style of the primary Stage
      * @param       primaryStageTitle                   Title of the primary Stage
      * @param       primaryStageWidth                   Width of the primary Stage
      * @param       primaryStageHeight                  Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(String fxmlResourceIndexPathString,
                           String fxmlResourcePrefixPathString,
                           Object mainApp,
                           Stage primaryStage,
                           StageStyle primaryStageStyle,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         Objects.nonNull(fxmlResourceIndexPathString);
 
         if (Files.exists(Paths.get(fxmlResourceIndexPathString)) == false) {
             logger.log(Level.SEVERE, "FXML index file not found");
 
             System.exit(1);
         }
 
         bindStage(Paths.get(fxmlResourceIndexPathString),
                   Paths.get(fxmlResourcePrefixPathString),
                   mainApp,
                   primaryStage,
                   primaryStageStyle,
                   primaryStageTitle,
                   primaryStageWidth,
                   primaryStageHeight);
     }
 
     /**
      * Binds the application Stage with StageManager's Stage with an option to toggle the use of
      * StageStyle.TRANSPARENT for the primary Stage
      * 
      * @param       fxmlResourceIndexPathString         String representation of the Path to the FXML index property file
      * @param       fxmlResourcePrefixPathString        String representation of the prefix Path of FXML files to be loaded
      * @param       mainApp                             The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                        The primary Stage of the application
      * @param       primaryStageTitle                   Title of the primary Stage
      * @param       primaryStageWidth                   Width of the primary Stage
      * @param       primaryStageHeight                  Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(String fxmlResourceIndexPathString,
                           String fxmlResourcePrefixPathString,
                           Object mainApp,
                           Stage primaryStage,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         bindStage(fxmlResourceIndexPathString,
                   fxmlResourcePrefixPathString,
                   mainApp,
                   primaryStage,
                   null,
                   primaryStageTitle,
                   primaryStageWidth,
                   primaryStageHeight);
     }
 
     /**
      * Binds the application Stage with StageManager's Stage with an option to toggle the use of
      * StageStyle.TRANSPARENT for the primary Stage
      * 
      * @param       fxmlResourceIndexPathString         String representation of the Path to the FXML index property file
      * @param       fxmlResourcePrefixPathString        String representation of the prefix Path of FXML files to be loaded
      * @param       mainApp                             The instance of the main pageNick point of the JavaFX application
      * @param       primaryStage                        The primary Stage of the application
      * @param       transparentStageStyle               If true, use StageStyle.TRANSPARENT for the primary Stage. Otherwise, use StageStyle.UNDECORATED
      * @param       primaryStageTitle                   Title of the primary Stage
      * @param       primaryStageWidth                   Width of the primary Stage
      * @param       primaryStageHeight                  Height of the primary Stage
      * 
      * @throws      MalformedFXMLIndexFileException     when the FXML index property file is malformed
      */
     public void bindStage(String fxmlResourceIndexPathString,
                           String fxmlResourcePrefixPathString,
                           Object mainApp,
                           Stage primaryStage,
                           boolean transparentStageStyle,
                           String primaryStageTitle,
                           double primaryStageWidth,
                           double primaryStageHeight) throws MalformedFXMLIndexFileException,
                                                             NoControllerSpecifiedException {
 
         StageStyle stageStyle;
 
         if (transparentStageStyle == true) {
             stageStyle = StageStyle.TRANSPARENT;
         } else {
             stageStyle = StageStyle.UNDECORATED;
         }
 
         bindStage(fxmlResourceIndexPathString,
                   fxmlResourcePrefixPathString,
                   mainApp,
                   primaryStage,
                   stageStyle,
                   primaryStageTitle,
                   primaryStageWidth,
                   primaryStageHeight);
     }
 
     /**
      * Sets the primary Stage MenuBar
      * 
      * @param       menuBar         MenuBar to be set
      */
     public void setMenuBar(MenuBar menuBar) {
         this.primaryStageMenuBar = menuBar;
 
         if (System.getProperty("os.name").toLowerCase().contains("mac")
             || System.getProperty("os.name").toLowerCase().contains("darwin")) {
 
             this.primaryStageMenuBar.setUseSystemMenuBar(true);
         }
     }
 
     /**
      * Gets XML attribute in the root parent
      * 
      * @param       xmlDocumentPath     Path to XML file
      * @param       nsPrefix            Attribute prefix
      * @param       attribute           Attribute name
      * 
      * @return  An Optional object containing value of the given attribute
      */
     private Optional<String> getRootXMLAttribute(Path xmlDocumentPath,
                                                  String nsPrefix,
                                                  String attribute) {
 
         Optional<String> result = Optional.empty();
 
         try (InputStream in = Files.newInputStream(xmlDocumentPath)) {
 
             DocumentBuilderFactory builders = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = builders.newDocumentBuilder();
             Document doc = builder.parse(in);
             Element xmlRoot = doc.getDocumentElement();
 
             if (nsPrefix == null) {
                 result = Optional.ofNullable(xmlRoot.getAttributeNode(attribute).getNodeValue());
             } else {
                 result = Optional.ofNullable(xmlRoot.getAttributeNode(nsPrefix + ':' + attribute).getNodeValue());
             }
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Got 'IOException' while reading FXML file: " + e.getMessage());
         } catch (ParserConfigurationException e) {
             logger.log(Level.SEVERE, "Got 'ParserConfigurationException' while reading FXML file: " + e.getMessage());
         } catch (SAXException e) {
             logger.log(Level.SEVERE, "Got 'SAXException' while reading FXML file: " + e.getMessage());
         }
 
         return result;
     }
 
     private Optional<List<String>> getNodeAttribute(Path xmlDocumentPath,
                                                     String tagName,
                                                     String nsPrefix,
                                                     String attribute,
                                                     int occurrences) {
 
         List<String> resultList = new CopyOnWriteArrayList<>();
         Optional<List<String>> result = Optional.of(resultList);
 
         try (InputStream in = Files.newInputStream(xmlDocumentPath, StandardOpenOption.READ)) {
 
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = dbf.newDocumentBuilder();
             Document doc = builder.parse(in);
 
             NodeList nodes = doc.getElementsByTagName(tagName);
 
             if (nodes.getLength() == 0) {
                 return Optional.empty();
             }
 
             for (int i = 0, lim = nodes.getLength(); i < lim; ++i) {
                 if (nodes.item(i).getAttributes().getNamedItem(nsPrefix + ":" + attribute) != null) {
                     resultList.add(nodes.item(i).getAttributes().getNamedItem(nsPrefix + ":" + attribute).getNodeValue());
 
                     if (--occurrences == 0) {
                         break;
                     }
                 } else if (lim - i == 1
                            && nodes.item(i).getAttributes().getNamedItem(nsPrefix + ":" + attribute) == null) {
                     result = Optional.empty();
                 }
             }
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Got 'IOException' while reading FXML file: " + e.getMessage());
         } catch (ParserConfigurationException e) {
             logger.log(Level.SEVERE, "Got 'ParserConfigurationException' while reading FXML file: " + e.getMessage());
         } catch (SAXException e) {
             logger.log(Level.SEVERE, "Got 'SAXException' while reading FXML file: " + e.getMessage());
         }
 
         return result;
     }
 
     /**
      * Defines page pageNick of the primary page to the first record in FXML index property file
      * 
      * @throws      PageNotFoundException       when the first record in FXML index property file define a page that is not in the page table
      * 
      * @see pageNick
      */
     public void autoDefineHomePage() throws PageNotFoundException {
         String parentProperty = new String();
 
         try (BufferedReader in = Files.newBufferedReader(fxmlResourceIndexPath, StandardCharsets.UTF_8)) {
 
             while (in.ready()) {
                 parentProperty = in.readLine();
 
                 if (parentProperty.length() == 0) {
                     continue;
                 } else if ((parentProperty.charAt(0) == COMMENT_SYMBOL) == false
                            && (parentProperty.charAt(0) == SpecialNick.SPECIAL_NICK_SYMBOL) == false) {
 
                     break;
                 }
             }
 
                 homePageNick = Optional.ofNullable(parentProperty.split("\\s*\\=")[0])
                                        .filter(s -> pageTable.containsKey(s))
                                        .get();
 
                 logger.log(Level.INFO, "Defined home page to '" + homePageNick + "'");
 
                 return;
         } catch (NoSuchElementException e) {
             logger.log(Level.SEVERE, "Attempting to define unrecognizable home page pageNick");
             throw new PageNotFoundException(parentProperty);
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Got IOException while attempting to define home page from FXML index file");
             throw new PageNotFoundException(parentProperty, e);
         }
     }
 
     /**
      * Defines home page pageNick to the first record in FXML index property file.
      * 
      * @param       pageNick          NickName of a page to be defined to
      * 
      * @throws      PageNotFoundException       when pageNick is not in the page table
      * 
      * @see pageNick
      */
     public boolean defineHomePageTo(String pageNick) throws PageNotFoundException {
         if (pageTable.containsKey(pageNick)) {
             this.homePageNick = pageNick;
 
             logger.log(Level.INFO, "Defined home page to '" + this.homePageNick + "'");
             return true;
         }
 
         logger.log(Level.SEVERE, "Attempting to define unrecognizable home page pageNick");
         throw new PageNotFoundException(pageNick);
     }
 
     /**
      * Adds a page to the page table
      * 
      * @param       pageNick                    NickName for the page to be added
      * @param       sceneResourcePathString     String representation of the Path to FXML resource
      * 
      * @throws      PageNotFoundException       when pageNick is not in the page table
      * 
      * @see pageTable
      * 
      * @todo draft
      */
     public void addPage(String pageNick,
                         String sceneResourcePathString,
                         Boolean inheritWidth,
                         Boolean inheritHeight) throws FileNotFoundException {
 
         if (pageTable.containsKey(pageNick)) {
             logger.log(Level.SEVERE, "page pageNick already exist: " + pageNick);
 
             return;
         }
 
         Path fxmlPath = fxmlResourcePrefixPath.resolve(sceneResourcePathString);
 
         if (Files.exists(fxmlPath)
             && Files.isRegularFile(fxmlPath)) {
 
             String controllerClassName;
             Class<?> controllerClass;
             Method getInstanceMethod;
             Object controllerInstance;
 
             FXMLLoader loader;
 
             try {
                 controllerClassName = new String();
                 loader = new FXMLLoader();
 
                 Optional<Double> prefWidth = Optional.of(
                     Double.valueOf(
                         getRootXMLAttribute(fxmlPath,
                                             null,
                                             FXMLAttribute.PANE_PREF_WIDTH.value).orElse("0.0")
                     )
                 );
 
                 Optional<Double> prefHeight = Optional.of(
                     Double.valueOf(
                         getRootXMLAttribute(fxmlPath,
                                             null,
                                             FXMLAttribute.PANE_PREF_HEIGHT.value).orElse("0.0")
                     )
                 );
 
                 controllerClassName = getRootXMLAttribute(fxmlPath,
                                                           FXMLLoader.FX_NAMESPACE_PREFIX,
                                                           FXMLLoader.FX_CONTROLLER_ATTRIBUTE).get();
 
                 controllerClass = Class.forName(controllerClassName);
 
                 if (controllerClass.isAnnotationPresent(StageManager.TheStageManager.class) == false) {
                     /**
                      * @danger DO NOT pass any message read from file to any string formatter
                      */
                     String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller of file '" + sceneResourcePathString + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                      + "FXML controller MUST be annotated with '@" + StageManager.TheStageManager.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageManager.TheStageManager.class.getCanonicalName() + "' which returns the singleton instance of the controller itself";
 
                     logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);
 
                     System.exit(1);
                 }
 
                 getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);
 
                 if (getInstanceMethod.isAnnotationPresent(StageManager.TheStageManager.class) == false) {
                     logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");
 
                     System.exit(1);
                 }
 
                 controllerInstance = getInstanceMethod.invoke(null);
 
                 loader.setLocation(fxmlPath.toUri().toURL());
                 loader.setController(controllerInstance);
 
                 pageTable.put(pageNick, new PageMap(loader.load(),
                                                     controllerClass,
                                                     prefWidth,
                                                     prefHeight,
                                                     inheritWidth,
                                                     inheritHeight));
 
                 logger.log(Level.INFO, "Page added: '" + pageNick + "' ::= '**/" + sceneResourcePathString + "'");
             } catch (IOException e) {
                 logger.log(Level.SEVERE, "Got 'IOException' while loading FXML resource: " + e.getMessage());
             } catch (ClassNotFoundException e) {
                 logger.log(Level.SEVERE, "Controller not found: " + e.getMessage());
             } catch (NoSuchMethodException e) {
                 logger.log(Level.SEVERE, "Method '" + GET_INSTANCE_METHOD_NAME + "' not found");
             } catch (IllegalAccessException e) {
                 /**
                  * An IllegalAccessException is thrown when an application tries to reflectively create an instance (other than an array),
                  * set or get a field, or invoke a method, but the currently executing method does not have access to the definition of
                  * the specified class, field, method or constructor.
                  */
                 logger.log(Level.SEVERE, "Got illegal access: " + e.getMessage());
             } catch (InvocationTargetException e) {
                 /**
                  * InvocationTargetException is a checked exception that wraps an exception thrown by an invoked method or constructor.
                  */
                 logger.log(Level.SEVERE, "Got an exception while getting instance of controller");
             }
         } else {
             throw new FileNotFoundException(sceneResourcePathString);
         }
     }
 
     /**
      * Removes a page from the page table
      * 
      * @param       pageNick          NickName for the page to be removed
      * 
      * @throws      PageNotFoundException       when pageNick is not in the page table
      * 
      * @see pageTable
      */
     public void removePage(String pageNick) throws PageNotFoundException {
         if (pageTable.containsKey(pageNick) == false) {
             logger.log(Level.SEVERE, "Attempting to remove page that is not in the page table");
             throw new PageNotFoundException(pageNick);
         }
 
         pageTable.remove(pageNick);
     }
 
     public Parent getPage(String pageNick) throws PageNotFoundException {
         if (pageTable.containsKey(pageNick) == false) {
             logger.log(Level.SEVERE, "Attempting to add a child to non existing page: " + pageNick);
 
             throw new PageNotFoundException(pageNick);
         }
 
         return pageTable.get(pageNick).parent;
     }
 
     public void clearContext() {
         this.context = null;
     }
 
     public Object getContext() {
         return context;
     }
 
     /**
      * Sets the current page
      * 
      * @param       stage                         Stage for page to be set
      * @param       pageNick                      NickName for a page in the page table to be used
      * @param       context                     An object to keep throughout Page transitions
      * 
      * @throws      PageNotFoundException         when pageNick is not in the page table
      * 
      * @see pageTable
      * @see addPage
      * @see removePage
      * 
      * @todo option to perform smooth Stage resizing
      * @bug Smooth Stage resizing animation task cancellation
      */
     public void setPage(Stage stage,
                         String pageNick, 
                         Object context) throws PageNotFoundException {
 
         if (pageNick == null) {
             logger.log(Level.SEVERE, "Home page has not been set");
             throw new PageNotFoundException();
         }
 
         if (pageTable.containsKey(pageNick) == false) {
             logger.log(Level.SEVERE, "Attempting to set unrecognized page pageNick '" + pageNick + "', current page will not be changed");
         }
 
         this.context = context;
 
         PageMap mapOfPageToSet = pageTable.get(pageNick);
 
         if (this.primaryStage.isFullScreen() == false) {
             if (mapOfPageToSet.inheritWidth == false) {
                 double prefWidth = mapOfPageToSet.prefWidth.get();
 
                 /**
                  * @bug
                  *
                  * final int P = 10;
                  *
                  * double d = prefWidth - stage.getWidth();
                  * double dx = d / P;
                  * long t = (long) Math.ceil(Math.abs(d) / P);
                  *
                  * Timer resize = new Timer();
                  *
                  * resize.scheduleAtFixedRate(new TimerTask() {
                  *
                  *     @Override
                  *     public void run() {
                  *
                  *         if (stage.getWidth() != prefWidth) {
                  *             stage.setWidth(stage.getWidth() + dx);
                  *         } else {
                  *             this.cancel();
                  *         }
                  *     }
                  * }, 0, t);
                  */
 
                 stage.setWidth(prefWidth);
                 this.primaryStageWidth = prefWidth;
                 // ((Node) this.primaryStageScenePage).prefWidth(prefWidth);
             }
 
             if (mapOfPageToSet.inheritHeight == false) {
                 double prefHeight = mapOfPageToSet.prefHeight.get();
                 stage.setHeight(this.titleBarHeight + prefHeight);
                 this.primaryStageHeight = this.titleBarHeight + prefHeight;
             }
         }
 
         if (this.primaryStageStyle == null
             || this.primaryStageStyle == StageStyle.DECORATED) {
 
             this.primaryStageScene.setRoot(pageTable.get(pageNick).parent);
         } else {
             /**
              * Children management for Stage with custom layout
              */
             ((StackPane) this.primaryStageScenePage.getChildren().get(1)).getChildren().remove(this.currentPrimaryStageScenePage);
             ((StackPane) this.primaryStageScenePage.getChildren().get(1)).getChildren().add(pageTable.get(pageNick).parent);
 
             this.currentPrimaryStageScenePage = pageTable.get(pageNick).parent;
 
             // 
             StackPane.setAlignment(((StackPane) this.primaryStageScenePage.getChildren().get(1)).getChildren().get(0), Pos.CENTER);
 
             AnchorPane.setBottomAnchor(pageTable.get(pageNick).parent, 0.0);
             this.primaryStageTitleBar.toFront();
         }
 
         if (this.alwaysCenteredStage) {
             this.primaryStage.centerOnScreen();
         }
 
         this.currentPrimaryStageScenePageNick.set(pageNick);
 
         logger.log(Level.INFO, "Current page has been set to '" + pageNick + "'");
     }
 
     /**
      * Sets the current page
      * 
      * @param       pageNick                      NickName for a page in the page table to be used
      * @param       context                     An object to keep throughout Page transitions
      * 
      * @throws      PageNotFoundException         when pageNick is not in the page table
      * 
      * @see pageTable
      * @see addPage
      * @see removePage
      * 
      * @todo option to perform smooth Stage resizing
      * @bug Smooth Stage resizing animation task cancellation
      */
     public void setPage(String pageNick, Object context) throws PageNotFoundException {
         setPage(this.primaryStage, pageNick, context);
     }
 
     /**
      * Add child element to page
      */
     public void addChildToPage(String pageNick,
                                Node child) throws PageNotFoundException {
 
         if (pageTable.containsKey(pageNick) == false) {
             logger.log(Level.SEVERE, "Attempting to add a child to non existing page: " + pageNick);
 
             throw new PageNotFoundException(pageNick);
         }
     }
 
     /**
      * Shows the primary Stage containing the home page
      * 
      * @param       context                     An object to keep throughout Page transitions
      * 
      * @throws      PageNotFoundException       when the home page has not been set
      * 
      * @see pageNick
      * @see defineHomeSceneTo
      */
     public void activate(Object context) throws PageNotFoundException {
         if (homePageNick == null) {
             logger.log(Level.SEVERE, "Cannot set initial page, home page has not been set");
 
             throw new PageNotFoundException();
         }
 
         if (primaryStageScene != null) {
             logger.log(Level.WARNING, "Stage is already active");
 
             return;
         }
 
         this.context = context;
 
         if (primaryStageStyle == null
             || primaryStageStyle == StageStyle.DECORATED) {
 
             this.primaryStageScene = new Scene(pageTable.get(homePageNick).parent,
                                                pageTable.get(homePageNick).prefWidth.get(),
                                                pageTable.get(homePageNick).prefHeight.get());
 
             this.primaryStage.initStyle(StageStyle.DECORATED);
             this.primaryStage.setTitle(this.primaryStageTitle);
             this.primaryStage.setScene(this.primaryStageScene);
         } else {
             craftPrimaryStage(primaryStageStyle);
         }
 
         this.primaryStage.show();
 
         if (this.alwaysCenteredStage) {
             this.primaryStage.centerOnScreen();
         }
 
         this.primaryStage.setFullScreenExitHint("กด ESC เพื่อออกจากโหมด Full-screen");
         this.primaryStage.setResizable(true);
         this.currentPrimaryStageScenePageNick.set(homePageNick);
 
         logger.log(Level.INFO, "Activated Stage");
     }
 
     /**
      * Activates a sub Stage
      */
     public void activateSubstage(Stage parentStage,
                                  String pageNick) throws PageNotFoundException {
 
         Objects.nonNull(parentStage);
         Objects.nonNull(pageNick);
 
         if (pageTable.containsKey(pageNick) == false) {
             throw new PageNotFoundException(pageNick);
         }
 
         Parent newSceneParent = new Pane();
         Scene newScene = new Scene(newSceneParent);
         Stage newStage = new Stage();
 
         parentStage.initModality(Modality.WINDOW_MODAL);
         parentStage.initOwner(parentStage);
     }
 
     /**
      * Activates a sub-Stage with its parent/owner set to the primaryStage
      * 
      * @param       pageNick          page pageNick of the constructing sub-Stage
      */
     public void activateSubstage(String pageNick) throws PageNotFoundException {
         activateSubstage(primaryStage, pageNick);
     }
 
     /**
      * Sets default Stage alignment on the current Screen
      * Defaults to true.
      * 
      * @param       alwaysCenteredStage     If true, Stages constructed by this StageManager will always positioned at the center of current Screen. Otherwise, Stage will never be re-positioned.
      */
     public void setAlwaysCenteredStage(boolean alwaysCenteredStage) {
         this.alwaysCenteredStage = alwaysCenteredStage;
     }
 
     /**
      * @section Custom Stage
      * 
      * @subsection  Custom Title Bar Specifications
      * A custom title bar MUST be a type that inherit @link javafx.scene.layout.HBox @unlink
      * and has at least one button that fire  'customCloseButton'.
      */
     static {
         CUSTOM_STAGE_STYLE = """
             -fx-background-radius: 11.0;
         """;
 
         CUSTOM_STAGE_CORNER_ARC = 22.0;
     }
 
     private void initializeSectionCustomStage() {
         visualBounds = Screen.getPrimary().getBounds();
         primaryStageTitlePosition = Pos.CENTER_LEFT;
     }
 
     /**
      * Style for platform-undecorated custom stage
      */
     private static final String CUSTOM_STAGE_STYLE;
 
     /**
      * Arc for custom Stage corner
      */
     private static final double CUSTOM_STAGE_CORNER_ARC;
 
     /**
      * Default height of the Stage title bar
      */
     private static final double DEFAULT_STAGE_TITLE_BAR_HEIGHT;
 
     /**
      * Visual bounds of the current screen
      */
     private Rectangle2D visualBounds;
 
     /**
      * Icon of the primary Stage
      */
     private Node primaryStageTitleIcon;
 
     /**
      * Title bar of the primary Stage
      */
     private Node primaryStageTitleBar;
 
     /**
      * Height of the custom Stage's title bar
      */
     private double titleBarHeight;
 
     /**
      * Custom title bar PageMap
      * 
      * @see setCustomTitleBarTo
      * 
      * @note In dev, currently unavailable
      * 
      * @todo setCustomTitleBarTo
      */
     private HBox customTitleBarNode;
 
     /**
      * Dragging context of the custom Stage
      */
     private TitleBar.StageDragContext stageDragContext;
 
     /**
      * Presence of custom close button assignment presence
      */
     private boolean hasCustomCloseButton;
 
     /**
      * Presence of custom close minimize button assignment presence
      */
     private boolean hasCustomMinimizeButton;
 
     /**
      * Presence of custom close full screen toggle button assignment
      */
     private boolean hasCustomFullScreenToggleButton;
 
     /**
      * Title of the primary Stage
      */
     private Pos primaryStageTitlePosition;
 
     /**
      * A tree holding the primary Stage hierarchy
      */
     private TreeMap<String, Stage> stageTree;
 
     /**
      * Alignments used in custom Stage
      */
     public static class Alignment {
 
         public static final int[] TOP_LEFT  = { 0, 0 };
 
         public static final int[] MIDDLE_TOP = { 1, 0 };
 
         public static final int[] TOP_RIGHT = { 2, 0 };
 
         public static final int CENTER      = 0b1111;
 
         public static final int TOP         = 0b0001;
 
         public static final int BOTTOM      = 0b0010;
 
         public static final int LEFT        = 0b0100;
 
         public static final int RIGHT       = 0b1000;
     }
 
     /**
      * Extended Stage
      */
     public class XStage
             extends Stage {
 
         private Parent titleBarPane;
 
         private Parent navBarPane;
 
         private Parent mainPage;
 
         private CopyOnWriteArrayList<Node> pages;
 
         public XStage() {
             this.titleBarPane = null;
             this.navBarPane = null;
         }
 
         public XStage(StageStyle style) {
             super(style);
 
             this.titleBarPane = null;
             this.navBarPane = null;
         }
 
         public XStage(StageStyle style,
                       Parent titleBarPane,
                       Parent navBarPane) {
 
             this(style);
 
             this.titleBarPane = titleBarPane;
             this.navBarPane = navBarPane;
         }
 
         public XStage(StageStyle style,
                       Parent navBarPane) {
 
             this(style, null, navBarPane);
         }
 
         public void addPages(Node... pages) {
             for (Node page : pages)
                 this.pages.add(page);
         }
 
         public void setMainPage(Parent mainPage) {
             this.mainPage = mainPage;
         }
     }
 
     /**
      * Thrown to indicate that the custom title bar FXML resource specification is invalid
      * 
      * @ref Custom_Title_Bar_Specifications
      */
     public final class InvalidCustomTitleBarException
             extends Exception {
 
         public InvalidCustomTitleBarException() {
         }
 
         public InvalidCustomTitleBarException(String message) {
             super(message);
         }
 
         public InvalidCustomTitleBarException(Throwable cause) {
             super(cause);
         }
 
         public InvalidCustomTitleBarException(String message,
                                               Throwable cause) {
 
             super(message, cause);
         }
 
         public InvalidCustomTitleBarException(String message,
                                               Throwable cause,
                                               boolean enableSuppression,
                                               boolean writableStackTrace) {
 
             super(message, cause, enableSuppression, writableStackTrace);
         }
     }
 
     /**
      * Sets the custom title bar of a custom Stage to pageNick
      * 
      * @note Custom title bar MUST inherited the type @link javafx.scene.layout.HBox @unlink
      * 
      * @param       customTitleBarParentPath              Path to the custom title bar FXML resource
      */
     public void setCustomTitleBarResourceTo(Path customTitleBarParentPath) throws FileNotFoundException,
                                                                                   InvalidCustomTitleBarException {
 
         if (Files.exists(customTitleBarParentPath) == false) {
             throw new FileNotFoundException(customTitleBarParentPath.toString());
         }
 
         Optional<List<String>> customButtonList = getNodeAttribute(customTitleBarParentPath,
                                                                    "Button",
                                                                    FXMLLoader.FX_NAMESPACE_PREFIX,
                                                                    FXMLLoader.FX_ID_ATTRIBUTE,
                                                                    0);
 
         if (customButtonList.isEmpty()) {
             throw new InvalidCustomTitleBarException("No such required Stage control Button assignments, at least a Button with fx:id 'customCloseButton'");
         }
 
         List<String> buttonIDList = customButtonList.get();
 
         int[] buttonIDCount = new int[3];
 
         for (String buttonID : buttonIDList) {
             if (buttonID.equals("customCloseButton")) {
                 buttonIDCount[0]++;
             } else if (buttonID.equals("customMinimizeButton")) {
                 buttonIDCount[1]++;
             } else if (buttonID.equals("customFullScreenToggleButton")) {
                 buttonIDCount[2]++;
             }
         }
 
         if (buttonIDCount[0] == 1) {
             hasCustomCloseButton = true;
         } else {
             throw new InvalidCustomTitleBarException("Required exactly one Stage control button with fx:id 'customCloseButton'");
         }
 
         if (buttonIDCount[1] == 1) {
             hasCustomMinimizeButton = true;
         } else if (buttonIDCount[1] > 1) {
             throw new InvalidCustomTitleBarException("Duplicate Stage control Button assignment");
         }
 
         if (buttonIDCount[2] == 1) {
             hasCustomFullScreenToggleButton = true;
         } else if (buttonIDCount[2] > 1) {
             throw new InvalidCustomTitleBarException("Duplicate Stage control Button assignment");
         }
 
         stageDragContext = new TitleBar.StageDragContext();
         Parent customTitleBarParent = null;
         FXMLLoader loader = new FXMLLoader();
 
         try {
             loader.setLocation(customTitleBarParentPath.toUri().toURL());
             loader.setController(new TitleBar(this.primaryStage));
 
             customTitleBarParent = loader.load();
         } catch (InvalidCustomTitleBarException e) {
             logger.log(Level.SEVERE, "Failed to automatically define custom title bar, invalid specs");
 
             System.exit(1);
         } catch (MalformedURLException e) {
             logger.log(Level.SEVERE, "Failed to set custom title bar to: '" + customTitleBarParentPath.toString() + "'");
 
             System.exit(1);
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Got 'IOException' while attempting to set custom title bar to: '" + customTitleBarParentPath.toString() + "'. Try checking the custom title bar specifications");
 
             System.exit(1);
         }
 
         if (customTitleBarParent.getClass().isAssignableFrom(HBox.class) == false) {
             throw new InvalidCustomTitleBarException(customTitleBarParentPath.toString());
         }
 
         customTitleBarParent.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(MouseEvent event) {
                 stageDragContext.offSetX = primaryStage.getX() - event.getScreenX();
                 stageDragContext.offSetY = primaryStage.getY() - event.getScreenY();
             }
         });
 
         customTitleBarParent.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(MouseEvent event) {
                 primaryStage.setX(event.getScreenX() + stageDragContext.offSetX);
                 primaryStage.setY(event.getScreenY() + stageDragContext.offSetY);
             }
         });
 
         this.customTitleBarNode = (HBox) customTitleBarParent;
     }
 
     /**
      * Sets the custom title bar of a custom Stage to pageNick
      * 
      * @note Custom title bar MUST inherited the type @link javafx.scene.layout.HBox @unlink
      * 
      * @param       customTitleBarParentPathString        String representation of Path to the custom title bar FXML resource
      * @throws InvalidCustomTitleBarException
      */
     public void setCustomTitleBarResourceTo(String customTitleBarParentPathString) throws FileNotFoundException,
                                                                                           InvalidCustomTitleBarException {
 
         Path customTitleBarPath = this.fxmlResourcePrefixPath.resolve(customTitleBarParentPathString);
 
         if (Files.exists(customTitleBarPath) == false) {
             throw new FileNotFoundException(customTitleBarParentPathString);
         }
 
         setCustomTitleBarResourceTo(customTitleBarPath);
     }
 
     /**
      * Defines custom title bar to a special nickName ':title_bar' from FXML index property file
      * @throws InvalidCustomTitleBarException
      * 
      * @note Custom title bar MUST inherited the type @link javafx.scene.layout.HBox @unlink
      * 
      */
     public void autoDefineCustomTitleBar() throws FileNotFoundException,
                                                   InvalidCustomTitleBarException {
 
         if (specialNickPool.getRecord(SpecialNick.SpecialNicks.CUSTOM_TITLE_BAR.nick).isPresent() == false) {
             logger.log(Level.SEVERE, "Failed to define custom title bar from FXML index property file");
 
             return;
         }
 
         Path customTitleBarPath = this.fxmlResourcePrefixPath.resolve(
             specialNickPool.getRecord(SpecialNick.SpecialNicks.CUSTOM_TITLE_BAR.nick).get().toString()
         );
 
         if (Files.exists(customTitleBarPath) == false) {
             throw new FileNotFoundException(customTitleBarPath.toString());
         }
 
         setCustomTitleBarResourceTo(customTitleBarPath);
     }
 
     /**
      * When using custom Stage, sets default Stage control button box alignment on the title bar
      * Defaults to true, which means, left-aligned.
      * 
      * @param       isStageControlButtonAlignLeft       If true sets the Stage control button box alignment to left. Otherwise, stick it to the left of title bar.
      */
     public void setStageControlButtonAlignLeft(boolean isStageControlButtonAlignLeft) {
         TitleBar.stageControlButtonBoxPositionLeft = isStageControlButtonAlignLeft;
     }
 
     /**
      * Craft a custom Stage
      * 
      * @param       stageStyle          StageStyle of the custom Stage
      */
     private void craftPrimaryStage(StageStyle stageStyle) {
         if (stageStyle != StageStyle.UNDECORATED
             && stageStyle != StageStyle.TRANSPARENT) {
 
             logger.log(Level.SEVERE, "Attempted to craft Stage with invalid StageStyle: '" + stageStyle.name() + "'");
 
             System.exit(1);
         }
 
         double prefPageHeight;
         double prefStageHeight;
 
         double prefPageWidth;
         double prefStageWidth;
 
         this.primaryStage.initStyle(stageStyle);
 
         if (this.pageTable.get(this.homePageNick).inheritHeight) {
             prefPageHeight = this.primaryStageHeight;
         } else {
             prefPageHeight = pageTable.get(homePageNick).prefHeight.get();
         }
 
         if (this.pageTable.get(this.homePageNick).inheritWidth) {
             prefPageWidth = this.primaryStageWidth;
         } else {
             prefPageWidth = pageTable.get(homePageNick).prefWidth.get();
         }
 
         prefStageWidth = prefPageWidth;
 
         AnchorPane rootNode = new AnchorPane();
         StackPane mainPageContent = new StackPane();
 
         clipChildren(rootNode, CUSTOM_STAGE_CORNER_ARC);
 
         this.primaryStage.fullScreenProperty().addListener((observer, oldValue, newValue) -> {
             Rectangle clipper = (Rectangle) primaryStageScenePage.getClip();
 
             if (newValue) {
                 clipper.setArcWidth(0);
                 clipper.setArcHeight(0);
             } else {
                 clipper.setArcWidth(CUSTOM_STAGE_CORNER_ARC);
                 clipper.setArcHeight(CUSTOM_STAGE_CORNER_ARC);
             }
 
             currentPrimaryStageScenePageContent.setPrefWidth(visualBounds.getWidth());
             currentPrimaryStageScenePageContent.setPrefHeight(primaryStageHeight - titleBarHeight);
         });
 
         HBox titleBar;
 
         if (this.specialNickPool.containsNick(SpecialNick.SpecialNicks.CUSTOM_TITLE_BAR.nick)) {
             if (this.customTitleBarNode == null) {
                 try {
                     autoDefineCustomTitleBar();
                 } catch (InvalidCustomTitleBarException e) {
                     logger.log(Level.SEVERE, e.getMessage());
 
                     System.exit(1);
                 } catch (FileNotFoundException e) {
                     logger.log(Level.SEVERE, "Failed to automatically define custom title bar");
 
                     System.exit(1);
                 }
             }
 
             titleBarHeight = this.customTitleBarNode.getPrefHeight();
             prefStageHeight = titleBarHeight + prefPageHeight;
 
             titleBar = (HBox) this.customTitleBarNode;
 
             titleBar.setPrefWidth(prefStageWidth);
             titleBar.setPrefHeight(titleBarHeight);
         } else {
             titleBarHeight = DEFAULT_STAGE_TITLE_BAR_HEIGHT;
             titleBar = new TitleBar(this.primaryStage,
                                     this.primaryStageTitle,
                                     prefStageWidth,
                                     titleBarHeight);
         }
 
         prefStageHeight = titleBarHeight + prefPageHeight;
 
         this.primaryStage.setHeight(prefStageHeight);
 
         rootNode.widthProperty().addListener((observer, oldValue, newValue) -> {
             if (titleBar instanceof TitleBar) {
                 TitleBar titleBarT = (TitleBar) titleBar;
                 titleBarT.setWidth(newValue);
             } else {
                 titleBar.setPrefWidth((double) newValue);
             }
 
             this.currentPrimaryStageScenePageContent.setPrefWidth((double) newValue);
         });
 
         rootNode.heightProperty().addListener((observer, oldValue, newValue) -> {
             this.currentPrimaryStageScenePageContent.setPrefHeight((double) newValue - titleBarHeight);
         });
 
         this.primaryStageTitleBar = titleBar;
 
         // MenuBar
         rootNode.getChildren().add(this.primaryStageMenuBar);
 
         // Title bar
         rootNode.getChildren().add(this.primaryStageTitleBar);
 
         // Page content
         mainPageContent.getChildren().add(pageTable.get(homePageNick).parent);
 
         /**
          * @important THIS IS IT!
          */
 
         /**
          * AnchorPane > StackPane
          */
 
         StackPane.setAlignment(pageTable.get(homePageNick).parent, Pos.CENTER);
 
         rootNode.getChildren().add(mainPageContent);
 
         this.primaryStageTitleBar.toFront();
 
         AnchorPane.setTopAnchor(this.primaryStageTitleBar, 0.0);
         AnchorPane.setBottomAnchor(mainPageContent, 0.0);
 
         Scene rootScene = new Scene(rootNode);
 
         rootScene.setFill(Color.TRANSPARENT);
         rootScene.setRoot(rootNode);
 
         mainPageContent.setBackground(new Background(new BackgroundFill(Color.valueOf("#000000"), null, null)));
 
         this.currentPrimaryStageScenePageContent = mainPageContent;
         this.currentPrimaryStageScenePage = pageTable.get(homePageNick).parent;
         this.primaryStageScene = rootScene;
         this.primaryStageScenePage = rootNode;
 
         rootNode.setPrefHeight(prefStageHeight);
 
         this.primaryStage.setScene(rootScene);
     }
 
     public final class TitleBar
             extends HBox {
 
         private final double BUTTON_PADDING = 8.0;
 
         private final Insets TITLE_BAR_ELEMENT_INSETS = new Insets(0, 10, 0, 10);
 
         private class StageControlButton
                 extends Button {
 
             public enum StageControlButtonType {
                 CLOSE_BUTTON,
                 MINIMIZE_BUTTON,
                 FULL_SCREEN_TOGGLE_BUTTON;
             }
 
             private final StageControlButtonType BUTTON_TYPE;
 
             public StageControlButton(StageControlButtonType buttonType) {
                 this.BUTTON_TYPE = buttonType;
             }
 
             public StageControlButton(String text,
                                       StageControlButtonType buttonType) {
 
                 super(text);
                 this.BUTTON_TYPE = buttonType;
             }
 
             public StageControlButton(String text,
                                       StageControlButtonType buttonType,
                                       Node graphic) {
 
                 super(text, graphic);
                 this.BUTTON_TYPE = buttonType;
             }
         }
 
         private static boolean stageControlButtonBoxPositionLeft;
 
         private final Insets STAGE_CONTROL_BUTTON_PADDING = new Insets(1, 1, 1, 1);
 
         private final String TITLE_BAR_COLOR = "-fx-background-color: #ffffff";
 
         private final String CLOSE_BUTTON_COLOR = "#ed6a5e";
 
         private final String MINIMIZE_BUTTON_COLOR = "#f5bf4f";
 
         private final String FULL_SCREEN_TOGGLE_BUTTON_COLOR = "#62c555";
 
         private Stage stage;
 
         private Robot robot;
 
         private static final double STAGE_CONTROL_BUTTON_RADIUS;
 
         private StackPane closeButtonStack;
 
         private StackPane minimizeButtonStack;
 
         private StackPane fullScreenToggleButtonStack;
 
         private Circle closeButton;
 
         private Circle minimizeButton;
 
         private Circle fullScreenToggleButton;
 
         private ImageView closeButtonLabel;
 
         private ImageView minimizeButtonLabel;
 
         private ImageView goFullScreenToggleButtonLabel;
 
         private ImageView goNormalScreenToggleButtonLabel;
 
         private final StageDragContext stageDragContext;
 
         private static final class StageDragContext {
 
             public double offSetX;
 
             public double offSetY;
         }
 
         private class TitleBarButtonEvent<T extends StageControlButton>
                     extends MouseEvent {
 
             public TitleBarButtonEvent(EventType<? extends MouseEvent> eventType,
                                        double x,
                                        double y,
                                        double screenX,
                                        double screenY,
                                        MouseButton button,
                                        int clickCount,
                                        boolean shiftDown,
                                        boolean controlDown,
                                        boolean altDown,
                                        boolean metaDown,
                                        boolean primaryButtonDown,
                                        boolean middleButtonDown,
                                        boolean secondaryButtonDown,
                                        boolean synthesized,
                                        boolean popupTrigger,
                                        boolean stillSincePress,
                                        PickResult pickResult) {
 
                 super(eventType,
                       x,
                       y,
                       screenX,
                       screenY,
                       button,
                       clickCount,
                       shiftDown,
                       controlDown,
                       altDown,
                       metaDown,
                       primaryButtonDown,
                       middleButtonDown,
                       secondaryButtonDown,
                       synthesized,
                       popupTrigger,
                       stillSincePress,
                       pickResult);
             }
 
             public TitleBarButtonEvent(EventType<? extends MouseEvent> eventType,
                                        double x,
                                        double y,
                                        double screenX,
                                        double screenY,
                                        MouseButton button,
                                        int clickCount,
                                        boolean shiftDown,
                                        boolean controlDown,
                                        boolean altDown,
                                        boolean metaDown,
                                        boolean primaryButtonDown,
                                        boolean middleButtonDown,
                                        boolean secondaryButtonDown,
                                        boolean backButtonDown,
                                        boolean forwardButtonDown,
                                        boolean synthesized,
                                        boolean popupTrigger,
                                        boolean stillSincePress,
                                        PickResult pickResult) {
 
                 super(eventType,
                       x,
                       y,
                       screenX,
                       screenY,
                       button,
                       clickCount,
                       shiftDown,
                       controlDown,
                       altDown,
                       metaDown,
                       primaryButtonDown,
                       middleButtonDown,
                       secondaryButtonDown,
                       backButtonDown,
                       forwardButtonDown,
                       synthesized,
                       popupTrigger,
                       stillSincePress,
                       pickResult);
             }
 
             public TitleBarButtonEvent(Object source,
                                        EventTarget target,
                                        EventType<? extends MouseEvent> eventType,
                                        double x,
                                        double y,
                                        double screenX,
                                        double screenY,
                                        MouseButton button,
                                        int clickCount,
                                        boolean shiftDown,
                                        boolean controlDown,
                                        boolean altDown,
                                        boolean metaDown,
                                        boolean primaryButtonDown,
                                        boolean middleButtonDown,
                                        boolean secondaryButtonDown,
                                        boolean synthesized,
                                        boolean popupTrigger,
                                        boolean stillSincePress,
                                        PickResult pickResult) {
 
                 super(source,
                       target,
                       eventType,
                       x,
                       y,
                       screenX,
                       screenY,
                       button,
                       clickCount,
                       shiftDown,
                       controlDown,
                       altDown,
                       metaDown,
                       primaryButtonDown,
                       middleButtonDown,
                       secondaryButtonDown,
                       synthesized,
                       popupTrigger,
                       stillSincePress,
                       pickResult);
             }
 
             public TitleBarButtonEvent(Object source,
                                        EventTarget target,
                                        EventType<? extends MouseEvent> eventType,
                                        double x,
                                        double y,
                                        double screenX,
                                        double screenY,
                                        MouseButton button,
                                        int clickCount,
                                        boolean shiftDown,
                                        boolean controlDown,
                                        boolean altDown,
                                        boolean metaDown,
                                        boolean primaryButtonDown,
                                        boolean middleButtonDown,
                                        boolean secondaryButtonDown,
                                        boolean backButtonDown,
                                        boolean forwardButtonDown,
                                        boolean synthesized,
                                        boolean popupTrigger,
                                        boolean stillSincePress,
                                        PickResult pickResult) {
 
                 super(source,
                       target,
                       eventType,
                       x,
                       y,
                       screenX,
                       screenY,
                       button,
                       clickCount,
                       shiftDown,
                       controlDown,
                       altDown,
                       metaDown,
                       primaryButtonDown,
                       middleButtonDown,
                       secondaryButtonDown,
                       backButtonDown,
                       forwardButtonDown,
                       synthesized,
                       popupTrigger,
                       stillSincePress,
                       pickResult);
             }
         }
 
         private EventHandler<MouseEvent> handleStageControlButtonBoxEnterHovering = e -> {
             closeButtonStack.getChildren().get(1).setVisible(true);
             minimizeButtonStack.getChildren().get(1).setVisible(true);
             if (StageManager.this.primaryStage.isFullScreen()) {
                 fullScreenToggleButtonStack.getChildren().get(1).setVisible(true);
             } else {
                 fullScreenToggleButtonStack.getChildren().get(2).setVisible(true);
             }
         };
 
         private EventHandler<MouseEvent> handleStageControlButtonBoxExitHovering = e -> {
             closeButtonStack.getChildren().get(1).setVisible(false);
             minimizeButtonStack.getChildren().get(1).setVisible(false);
             fullScreenToggleButtonStack.getChildren().get(1).setVisible(false);
             fullScreenToggleButtonStack.getChildren().get(2).setVisible(false);
         };
 
         private EventHandler<MouseEvent> handlePressCloseButton = e -> {
             closeButton.setOpacity(60);
         };
 
         private EventHandler<MouseEvent> handlePressMinimizeButton = e -> {
             minimizeButton.setOpacity(60);
         };
 
         private EventHandler<MouseEvent> handlePressFullScreenToggleButton = e -> {
             fullScreenToggleButton.setOpacity(60);
         };
 
         private EventHandler<MouseEvent> handleOnCloseRequest = e -> {
             stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
         };
 
         private EventHandler<MouseEvent> handleOnMinimizeRequest = e -> {
             stage.setIconified(true);
         };
 
         private EventHandler<MouseEvent> handleOnFullScreenToggleRequest = e -> {
             if (stage.isFullScreen()) {
                 stage.setFullScreen(false);
                 stage.getScene().getRoot().setStyle(CUSTOM_STAGE_STYLE);
             } else {
                 currentPrimaryStageScenePageContent.setPrefWidth(visualBounds.getWidth());
                 currentPrimaryStageScenePageContent.setPrefHeight(primaryStageHeight - titleBarHeight);
 
                 stage.setFullScreen(true);
                 stage.getScene().getRoot().setStyle(null);
             }
         };
 
         @FXML
         private Button customCloseButton;
 
         @FXML
         private Button customMinimizeButton;
 
         @FXML
         private Button customFullScreenToggleButton;
 
         static {
             stageControlButtonBoxPositionLeft = true;
             STAGE_CONTROL_BUTTON_RADIUS = 6.5;
         }
 
         private TitleBar(Stage stage) throws InvalidCustomTitleBarException {
             this.stage = stage;
 
             stageDragContext = new StageDragContext();
         }
 
         private TitleBar(Stage stage,
                          String stageTitle,
                          double width,
                          double height) {
 
             this.stage = stage;
 
             stageDragContext = new StageDragContext();
 
             super.setPrefWidth(width);
             super.setPrefHeight(height);
             super.setStyle(TITLE_BAR_COLOR);
 
             super.setFillHeight(true);
             super.setMaxWidth(Double.MAX_VALUE);
 
             HBox stageGrabBarBox = createStageGrabBarBox(primaryStageTitleIcon, stageTitle);
             HBox stageControlButtonBox = createStageControlButtonBox();
 
             if (stageControlButtonBoxPositionLeft) {
                 super.getChildren().add(stageControlButtonBox);
                 super.getChildren().add(stageGrabBarBox);
 
                 super.setAlignment(Pos.CENTER_LEFT);
             } else {
                 super.getChildren().add(stageGrabBarBox);
                 super.getChildren().add(stageControlButtonBox);
 
                 super.setAlignment(Pos.CENTER_RIGHT);
             }
 
             HBox.setHgrow(stageGrabBarBox, Priority.ALWAYS);
         }
 
         private HBox createStageControlButtonBox() {
             HBox stageControlButtonBox = new HBox();
 
             closeButtonStack = new StackPane();
             minimizeButtonStack = new StackPane();
             fullScreenToggleButtonStack = new StackPane();
 
             Image closeButtonLabelImage = null;
             Image minimizeButtonLabelImage = null;
             Image goFullScreenToggleButtonLabelImage = null;
             Image goNormalScreenToggleButtonLabelImage = null;
 
             try (
                 InputStream closeButtonLabelImageIn = Files.newInputStream(OakResourcePrefix.getPrefix().resolve(OakAppConfigs.getProperty(OakAppDefaults.IMAGE_DIR.key())).resolve(OakAppConfigs.getProperty("app.ui.icon.closeButtonLabelImage")));
                 InputStream minimizeButtonLabelImageIn = Files.newInputStream(OakResourcePrefix.getPrefix().resolve(OakAppConfigs.getProperty(OakAppDefaults.IMAGE_DIR.key())).resolve(OakAppConfigs.getProperty("app.ui.icon.minimizeButtonLabelImage")));
                 InputStream goFullScreenToggleButtonLabelImageIn = Files.newInputStream(OakResourcePrefix.getPrefix().resolve(OakAppConfigs.getProperty(OakAppDefaults.IMAGE_DIR.key())).resolve(OakAppConfigs.getProperty("app.ui.icon.goFullScreenToggleButtonLabelImage")));
                 InputStream goNormalScreenToggleButtonLabelImageIn = Files.newInputStream(OakResourcePrefix.getPrefix().resolve(OakAppConfigs.getProperty(OakAppDefaults.IMAGE_DIR.key())).resolve(OakAppConfigs.getProperty("app.ui.icon.goNormalScreenToggleButtonLabelImage")))
             ) {
                 closeButtonLabelImage = new Image(closeButtonLabelImageIn);
                 minimizeButtonLabelImage = new Image(minimizeButtonLabelImageIn);
                 goFullScreenToggleButtonLabelImage = new Image(goFullScreenToggleButtonLabelImageIn);
                 goNormalScreenToggleButtonLabelImage = new Image(goNormalScreenToggleButtonLabelImageIn);
             } catch (IOException e) {
                 logger.log(Level.SEVERE, "Got 'IOException' while attempting to load Stage control button label");
             }
 
             closeButtonLabel = new ImageView();
             minimizeButtonLabel = new ImageView();
             goFullScreenToggleButtonLabel = new ImageView();
             goNormalScreenToggleButtonLabel = new ImageView();
 
             closeButtonLabel.setImage(closeButtonLabelImage);
             minimizeButtonLabel.setImage(minimizeButtonLabelImage);
             goFullScreenToggleButtonLabel.setImage(goFullScreenToggleButtonLabelImage);
             goNormalScreenToggleButtonLabel.setImage(goNormalScreenToggleButtonLabelImage);
 
             closeButtonLabel.setOpacity(90);
             minimizeButtonLabel.setOpacity(90);
             goFullScreenToggleButtonLabel.setOpacity(90);
             goNormalScreenToggleButtonLabel.setOpacity(90);
 
             closeButtonLabel.setFitWidth(STAGE_CONTROL_BUTTON_RADIUS - 0.5);
             minimizeButtonLabel.setFitWidth(STAGE_CONTROL_BUTTON_RADIUS - 0.5);
             goFullScreenToggleButtonLabel.setFitWidth(STAGE_CONTROL_BUTTON_RADIUS);
             goNormalScreenToggleButtonLabel.setFitWidth(STAGE_CONTROL_BUTTON_RADIUS);
 
             closeButtonLabel.setPreserveRatio(true);
             minimizeButtonLabel.setPreserveRatio(true);
             goFullScreenToggleButtonLabel.setPreserveRatio(true);
             goNormalScreenToggleButtonLabel.setPreserveRatio(true);
 
             closeButton = new Circle();
             minimizeButton = new Circle();
             fullScreenToggleButton = new Circle();
 
             closeButton.setRadius(STAGE_CONTROL_BUTTON_RADIUS);
             minimizeButton.setRadius(STAGE_CONTROL_BUTTON_RADIUS);
             fullScreenToggleButton.setRadius(STAGE_CONTROL_BUTTON_RADIUS);
 
             closeButton.setFill(Color.valueOf(CLOSE_BUTTON_COLOR));
             minimizeButton.setFill(Color.valueOf(MINIMIZE_BUTTON_COLOR));
             fullScreenToggleButton.setFill(Color.valueOf(FULL_SCREEN_TOGGLE_BUTTON_COLOR));
 
             closeButtonStack.getChildren().add(0, closeButton);
             closeButtonStack.getChildren().add(1, closeButtonLabel);
 
             minimizeButtonStack.getChildren().add(0, minimizeButton);
             minimizeButtonStack.getChildren().add(1, minimizeButtonLabel);
 
             fullScreenToggleButtonStack.getChildren().add(0, fullScreenToggleButton);
             fullScreenToggleButtonStack.getChildren().add(1, goNormalScreenToggleButtonLabel);
             fullScreenToggleButtonStack.getChildren().add(2, goFullScreenToggleButtonLabel);
 
             closeButtonStack.getChildren().get(1).setVisible(false);
             minimizeButtonStack.getChildren().get(1).setVisible(false);
             fullScreenToggleButtonStack.getChildren().get(1).setVisible(false);
             fullScreenToggleButtonStack.getChildren().get(2).setVisible(false);
 
             stageControlButtonBox.addEventHandler(MouseEvent.MOUSE_ENTERED, handleStageControlButtonBoxEnterHovering);
             stageControlButtonBox.addEventHandler(MouseEvent.MOUSE_EXITED, handleStageControlButtonBoxExitHovering);
 
             closeButtonStack.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnCloseRequest);
             minimizeButtonStack.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnMinimizeRequest);
             fullScreenToggleButtonStack.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnFullScreenToggleRequest);
 
             closeButtonStack.addEventHandler(MouseEvent.MOUSE_PRESSED, handlePressCloseButton);
             minimizeButtonStack.addEventHandler(MouseEvent.MOUSE_PRESSED, handlePressMinimizeButton);
             fullScreenToggleButtonStack.addEventHandler(MouseEvent.MOUSE_PRESSED, handlePressFullScreenToggleButton);
 
             closeButtonStack.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
 
                 @Override
                 public void handle(MouseEvent event) {
                     closeButton.setFill(Color.valueOf(CLOSE_BUTTON_COLOR));
                 }
             });
 
             minimizeButtonStack.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
 
                 @Override
                 public void handle(MouseEvent event) {
                     minimizeButton.setFill(Color.valueOf(MINIMIZE_BUTTON_COLOR));
                 }
             });
 
             fullScreenToggleButtonStack.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
 
                 @Override
                 public void handle(MouseEvent event) {
                     fullScreenToggleButton.setFill(Color.valueOf(FULL_SCREEN_TOGGLE_BUTTON_COLOR));
                 }
             });
 
             stageControlButtonBox.setPadding(TITLE_BAR_ELEMENT_INSETS);
 
             stageControlButtonBox.setAlignment(Pos.CENTER);
 
             // Whether to use custom or default
             if (stageControlButtonBoxPositionLeft) {
                 stageControlButtonBox.getChildren().add(closeButtonStack);
                 stageControlButtonBox.getChildren().add(minimizeButtonStack);
                 stageControlButtonBox.getChildren().add(fullScreenToggleButtonStack);
             } else {
                 stageControlButtonBox.getChildren().add(minimizeButtonStack);
                 stageControlButtonBox.getChildren().add(fullScreenToggleButtonStack);
                 stageControlButtonBox.getChildren().add(closeButtonStack);
             }
 
             stageControlButtonBox.setSpacing(BUTTON_PADDING);
 
             return stageControlButtonBox;
         }
 
         private HBox createStageGrabBarBox(Node stageIcon,
                                            String stageTitle) {
 
             HBox stageGrabBarBox = new HBox();
 
             Node stageIconNode = createStageTitleIconNode(stageIcon);
             Text stageTitleText = createStageTitleText(stageTitle);
 
             if (stageIconNode != null) {
                 stageGrabBarBox.getChildren().add(stageIconNode);
             }
 
             stageGrabBarBox.getChildren().add(stageTitleText);
             stageGrabBarBox.setAlignment(Pos.CENTER_LEFT);
             stageGrabBarBox.setPadding(TITLE_BAR_ELEMENT_INSETS);
 
             HBox.setHgrow(stageTitleText, Priority.ALWAYS);
 
             stageGrabBarBox.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
 
                 @Override
                 public void handle(MouseEvent event) {
                     stageDragContext.offSetX = primaryStage.getX() - event.getScreenX();
                     stageDragContext.offSetY = primaryStage.getY() - event.getScreenY();
                 }
             });
 
             stageGrabBarBox.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
 
                 @Override
                 public void handle(MouseEvent event) {
                     primaryStage.setX(event.getScreenX() + stageDragContext.offSetX);
                     primaryStage.setY(event.getScreenY() + stageDragContext.offSetY);
                 }
             });
 
             return stageGrabBarBox;
         }
 
         private Text createStageTitleText(String stageTitle) {
             Text titleText = new Text(stageTitle);
 
             titleText.setFont(currentFont);
 
             return titleText;
         }
 
         private Node createStageTitleIconNode(Node icon) {
             return icon;
         }
 
         public void setWidth(Number width) {
             super.setPrefWidth((double) width);
         }
 
         public void setHeight(Number height) {
             super.setPrefHeight((double) height);
         }
 
         @FXML
         public void initialize() {
             customCloseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnCloseRequest);
 
             if (hasCustomMinimizeButton) {
                 customMinimizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnMinimizeRequest);
             }
 
             if (hasCustomFullScreenToggleButton) {
                 customFullScreenToggleButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handleOnFullScreenToggleRequest);
             }
         }
     }
 
     /**
      * Masks children of the region with the region itself
      */
     private static void clipChildren(Region region,
                                      double arc) {
 
         final Rectangle rootRegion = new Rectangle();
 
         rootRegion.setArcWidth(arc);
         rootRegion.setArcHeight(arc);
         region.setClip(rootRegion);
 
         region.layoutBoundsProperty().addListener((observer, oldValue, newValue) -> {
             rootRegion.setWidth(newValue.getWidth());
             rootRegion.setHeight(newValue.getHeight());
         });
     }
 
     /**
      * @section Draggability
      * 
      * @subsection References
      * 
      * - Draggable Panels Example
      *   https://docs.oracle.com/javase/8/javafx/events-tutorial/draggablepanelsexamplejava.htm
      */
     static {}
 
     private void initializeSectionDraggability() {}
 
     /**
      * The annotation used to annontate states that SHOULD be Draggable
      * 
      * @note In dev, currently unavailable
      */
     @Inherited
     @Retention (RetentionPolicy.RUNTIME)
     @Target (ElementType.FIELD)
     public @interface Draggable {}
 
     /**
      * Property holding drag mode status
      */
     private SimpleBooleanProperty dragModeActiveProperty = new SimpleBooleanProperty(this.primaryStage, "dragModeActive", true);
 
     /**
      * Mouse dragging context
      */
     private static final class DragContext {
 
         public double mouseAnchorX;
 
         public double mouseAnchorY;
 
         public double initialTranslateX;
 
         public double initialTranslateY;
     }
 
     /**
      * Make a Node draggable
      */
     public <T extends Node> Node makeDraggable(final T node) {
         final DragContext dragContext = new DragContext();
         final Group wrapGroup = new Group(node);
 
         wrapGroup.maxWidth(Double.MAX_VALUE);
 
         wrapGroup.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(final MouseEvent mouseEvent) {
                 if (dragModeActiveProperty.get()) {
                     mouseEvent.consume();
                 }
             }
         });
 
         wrapGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(final MouseEvent mouseEvent) {
                 if (dragModeActiveProperty.get()) {
                     dragContext.mouseAnchorX = mouseEvent.getX();
                     dragContext.mouseAnchorY = mouseEvent.getY();
 
                     dragContext.initialTranslateX = node.getTranslateX();
                     dragContext.initialTranslateY = node.getTranslateY();
                 }
             }
         });
 
         wrapGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
 
             @Override
             public void handle(final MouseEvent mouseEvent) {
                 if (dragModeActiveProperty.get()) {
                     node.setTranslateX(dragContext.initialTranslateX
                                        + mouseEvent.getX()
                                        - dragContext.mouseAnchorX);
                     node.setTranslateY(dragContext.initialTranslateY
                                        + mouseEvent.getY()
                                        - dragContext.mouseAnchorY);
                 }
             }
         });
 
         return wrapGroup;
     }
 
     /**
      * @section Fonts
      */
 
     /**
      * Set containing all expected Font resource content type; MIME
      */
     private static Set<String> FONT_CONTENT_TYPE_ARRAY;
 
     static {
         FONT_CONTENT_TYPE_ARRAY = new TreeSet<>();
 
         FONT_CONTENT_TYPE_ARRAY.add("font/ttf");
         FONT_CONTENT_TYPE_ARRAY.add("font/otf");
         FONT_CONTENT_TYPE_ARRAY.add("font/woff");
         FONT_CONTENT_TYPE_ARRAY.add("font/woff2");
     }
 
     private void initializeSectionFont() {
         fontTable = new ConcurrentHashMap<>();
         currentFont = new Font("System", 14);
     }
 
     /**
      * Font resource table
      */
     private static Map<String, Font> fontTable;
 
     /**
      * Current Font
      * 
      * @note Use during development
      */
     private Font currentFont;
 
     /**
      * Loads Fonts from a directory
      * 
      * @param       fontDirPath     Directory containing all the Fonts to be loaded
      * @param       fontSize        Default Font size
      * 
      * @throws      FileNotFoundException       when Path does not exist
      * @throws      NotDirectoryException       when Path does not point to a directory
      */
     public void loadFontsFrom(Path fontDirPath,
                               double fontSize) throws FileNotFoundException,
                                                       NotDirectoryException {
 
         if (Files.exists(fontDirPath) == false) {
             logger.log(Level.SEVERE, "Font directory does not exist: " + fontDirPath.toString());
 
             throw new FileNotFoundException(fontDirPath.toString());
         } else if (Files.isDirectory(fontDirPath) == false) {
             logger.log(Level.SEVERE, "Not directory: " + fontDirPath.toString());
 
             throw new NotDirectoryException(fontDirPath.toString());
         }
 
         int fontLoadCount = 0;
 
         try (DirectoryStream<Path> dirs = Files.newDirectoryStream(fontDirPath)) {
 
             for (Path entry : dirs) {
                 if (!OakSystemDefaults.OS_NAME.value().toLowerCase().contains("windows")
                      && FONT_CONTENT_TYPE_ARRAY.contains(Files.probeContentType(entry)) == false) {
 
                     continue;
                 }
 
                 try (InputStream in = Files.newInputStream(entry)) {
 
                     Font font = Font.loadFont(in, fontSize);
 
                     fontTable.put(font.getName(), font);
 
                     fontLoadCount++;
                     logger.log(Level.INFO, "Loaded font: '" + font.getName() + "', size: " + font.getSize());
                 }
             }
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Cannot reach font directory, got 'IOException'");
 
             System.exit(1);
         } finally {
             logger.log(Level.INFO, Integer.toString(fontLoadCount) + " font" + (fontLoadCount == 1 ? "" : "s") + " loaded");
         }
     }
 
     /**
      * Gets the unmodifiable Font table containing all loaded Fonts
      * 
      * @see loadFontsFrom
      */
     public Map<String, Font> getFontTable() {
         return Collections.unmodifiableMap(fontTable);
     }
 }
