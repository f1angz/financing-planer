package org.example.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.stage.Stage;

/**
 * Утилита для применения темной темы к title bar в Windows
 */
public class WindowsThemeUtil {
    
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
    
    /**
     * Применяет темную тему к title bar окна (только для Windows 10/11)
     */
    public static void setDarkTitleBar(Stage stage) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            return; // Работает только на Windows
        }
        
        try {
            // Ждем пока окно полностью инициализируется
            stage.show();
            
            // Получаем дескриптор окна
            String title = stage.getTitle();
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, title);
            
            if (hwnd != null) {
                // Пробуем оба варианта атрибута (для разных версий Windows)
                setDarkMode(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE);
                setDarkMode(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1);
            }
            
        } catch (Exception e) {
            System.err.println("Не удалось применить темную тему title bar: " + e.getMessage());
        }
    }
    
    private static void setDarkMode(WinDef.HWND hwnd, int attribute) {
        try {
            // Загружаем библиотеку dwmapi.dll
            DwmApi dwmApi = Native.load("dwmapi", DwmApi.class);
            
            // Устанавливаем темный режим (TRUE = 1)
            WinDef.BOOLByReference darkMode = new WinDef.BOOLByReference(new WinDef.BOOL(true));
            dwmApi.DwmSetWindowAttribute(
                hwnd, 
                attribute, 
                darkMode.getPointer(), 
                WinDef.BOOL.SIZE
            );
            
        } catch (Exception e) {
            // Игнорируем ошибки для совместимости со старыми версиями Windows
        }
    }
    
    /**
     * Интерфейс для работы с DWM API Windows
     */
    private interface DwmApi extends Library {
        int DwmSetWindowAttribute(
            WinDef.HWND hwnd,
            int dwAttribute,
            Pointer pvAttribute,
            int cbAttribute
        );
    }
}

