package jp.toastkid.yobidashi.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * In App Browser's circular menu.
 *
 * @param titleId Menu title resource ID
 * @param iconId Menu icon resource ID
 * @author toastkidjp
 */
enum class Menu(
        @param:StringRes val titleId: Int,
        @param:DrawableRes val iconId: Int
) {

    TAB_LIST(R.string.title_tab_list, R.drawable.ic_tab),

    TOP(R.string.title_menu_to_top, R.drawable.ic_top),

    BOTTOM(R.string.title_menu_to_bottom, R.drawable.ic_bottom),

    BACK(R.string.title_menu_back, R.drawable.ic_back),

    FORWARD(R.string.title_menu_forward, R.drawable.ic_forward),

    RELOAD(R.string.title_menu_reload, R.drawable.ic_reload),

    STOP_LOADING(R.string.title_stop_loading, R.drawable.ic_close),

    READER_MODE(R.string.title_menu_reader_mode, R.drawable.ic_reader_mode),

    PAGE_INFORMATION(R.string.title_menu_page_information, R.drawable.ic_info),

    USER_AGENT(R.string.title_user_agent, R.drawable.ic_user_agent),

    WIFI_SETTING(R.string.title_settings_wifi, R.drawable.ic_wifi),

    FIND_IN_PAGE(R.string.title_find_in_page, R.drawable.ic_find_in_page),

    ARCHIVE(R.string.title_archive, R.drawable.ic_archive),

    VIEW_ARCHIVE(R.string.title_archives, R.drawable.ic_view_archive),

    SHARE(R.string.section_title_share, R.drawable.ic_share),

    REPLACE_HOME(R.string.title_replace_home, R.drawable.ic_replace_home),

    LOAD_HOME(R.string.title_load_home, R.drawable.ic_home),

    VIEW_HISTORY(R.string.title_view_history, R.drawable.ic_history),

    BOOKMARK(R.string.title_bookmark, R.drawable.ic_bookmark),

    ADD_BOOKMARK(R.string.title_add_bookmark, R.drawable.ic_add_bookmark),

    VOICE_SEARCH(R.string.title_voice_search, R.drawable.ic_mic),

    WEB_SEARCH(R.string.title_search, R.drawable.ic_search_white),

    RANDOM_WIKIPEDIA(R.string.menu_random_wikipedia, R.drawable.ic_wikipedia_white),

    SETTING(R.string.title_settings, R.drawable.ic_settings),

    EDITOR(R.string.title_editor, R.drawable.ic_edit),

    PDF(R.string.title_open_pdf, R.drawable.ic_pdf),

    CODE_READER(R.string.title_code_reader, R.drawable.ic_barcode),

    SCHEDULE(R.string.title_calendar, R.drawable.ic_schedule),

    CAMERA(R.string.title_camera, R.drawable.ic_camera),

    PLANNING_POKER(R.string.title_planning_poker, R.drawable.ic_card),

    TORCH(R.string.title_torch, R.drawable.ic_light),

    APP_LAUNCHER(R.string.title_apps_launcher, R.drawable.ic_launcher),

    OVERLAY_COLOR_FILTER(R.string.title_filter_color, R.drawable.ic_color_filter),

    CALCULATOR(R.string.title_calculator, R.drawable.ic_calculator),

    ABOUT(R.string.title_about_this_app, R.drawable.ic_yobidashi),

    EXIT(R.string.exit, R.drawable.ic_exit)
    ;

}