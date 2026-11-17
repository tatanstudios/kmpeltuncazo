import SwiftUI
import UIKit
import ComposeApp
import OneSignalFramework

/// App de OneSignal (no es secreta: va dentro de la app).
private let oneSignalAppId = "757d1cff-cdc8-4b18-a332-e1cd7bae3eb5"

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        OneSignal.initialize(oneSignalAppId, withLaunchOptions: launchOptions)

        // el código compartido (Kotlin) vincula al cliente al iniciar o cerrar sesión
        Notificaciones.shared.puente = PuenteOneSignal()
        return true
    }
}

/// Implementa la interfaz de Kotlin `PuenteNotificaciones` con el SDK de OneSignal.
final class PuenteOneSignal: NSObject, PuenteNotificaciones {
    func iniciarSesion(idCliente: String) {
        OneSignal.login(idCliente)
    }

    func cerrarSesion() {
        OneSignal.logout()
    }

    func pedirPermiso() {
        // solo muestra el aviso del sistema si el cliente aún no ha respondido
        DispatchQueue.main.async {
            OneSignal.Notifications.requestPermission({ _ in }, fallbackToSettings: false)
        }
    }
}
