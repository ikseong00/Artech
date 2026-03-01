package org.ikseong.devnews.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.ikseong.devnews.BuildKonfig

object SupabaseProvider {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildKonfig.SUPABASE_URL,
        supabaseKey = BuildKonfig.SUPABASE_KEY,
    ) {
        install(Postgrest)
    }
}
