package dev.limebeck.libs.docker.client.dslUtils

import kotlin.jvm.JvmName
import kotlin.reflect.KProperty

interface ApiCacheHolder {
    val apiCache: MutableMap<Any, Any>
}

class ApiDelegate<T : Any, R : dev.limebeck.libs.docker.client.dslUtils.ApiCacheHolder>(val factory: (R) -> T) {
    operator fun getValue(thisRef: R, property: KProperty<*>): T {
        return thisRef.apiCache.getOrPut(property.name) {
            factory(thisRef)
        } as T
    }
}

fun <T : Any, R : dev.limebeck.libs.docker.client.dslUtils.ApiCacheHolder> api(factory: (R) -> T) =
    _root_ide_package_.dev.limebeck.libs.docker.client.dslUtils.ApiDelegate(factory)

typealias ApiFactory<T, R> = (R) -> T

@JvmName("apiDelegateExtension")
fun <T : Any, R : dev.limebeck.libs.docker.client.dslUtils.ApiCacheHolder> dev.limebeck.libs.docker.client.dslUtils.ApiFactory<T, R>.api() =
    _root_ide_package_.dev.limebeck.libs.docker.client.dslUtils.ApiDelegate(this)
