package tool.xfy9326.naucourse.utils.views

import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.network.clients.base.LoginResponse.ErrorReason
import tool.xfy9326.naucourse.providers.beans.GeneralNews.PostSource
import tool.xfy9326.naucourse.providers.beans.jwc.StudentLearningProcess.CourseType
import tool.xfy9326.naucourse.providers.beans.jwc.StudentLearningProcess.SubjectType
import tool.xfy9326.naucourse.providers.contents.base.ContentErrorReason
import tool.xfy9326.naucourse.ui.models.activity.LoginViewModel.LoadingProcess
import tool.xfy9326.naucourse.ui.models.activity.SchoolCalendarViewModel.CalendarLoadStatus
import tool.xfy9326.naucourse.ui.models.activity.ScoreQueryViewModel.CreditCountStatus
import tool.xfy9326.naucourse.ui.models.fragment.CourseArrangeViewModel.CourseArrangeNotifyType
import tool.xfy9326.naucourse.ui.models.fragment.CourseTableViewModel.CurrentWeekStatus
import java.util.concurrent.TimeUnit

object I18NUtils {

    fun getCreditCountStatusResId(status: CreditCountStatus): Int =
        when (status) {
            CreditCountStatus.DATA_LOADING -> R.string.operation_when_data_loading
            CreditCountStatus.EMPTY_DATA -> R.string.empty_credit_count_data
        }

    fun getTimeUnitResId(timeUnit: TimeUnit): Int =
        when (timeUnit) {
            TimeUnit.DAYS -> R.string.time_unit_day
            TimeUnit.HOURS -> R.string.time_unit_hour
            TimeUnit.MINUTES -> R.string.time_unit_minute
            else -> error("Unsupported I18N Time Unit")
        }

    fun getCalendarLoadStatusResId(calendarLoadStatus: CalendarLoadStatus): Int =
        when (calendarLoadStatus) {
            CalendarLoadStatus.LOADING_IMAGE_LIST -> R.string.loading_calendar_list
            CalendarLoadStatus.IMAGE_LIST_LOAD_FAILED -> R.string.loading_calendar_list_failed
            CalendarLoadStatus.IMAGE_LOAD_FAILED -> R.string.loading_calendar_failed
        }

    fun getCurrentWeekStatusResId(currentWeekStatus: CurrentWeekStatus): Int =
        when (currentWeekStatus) {
            CurrentWeekStatus.IN_VACATION -> R.string.in_vacation
            CurrentWeekStatus.IS_CURRENT_WEEK -> R.string.current_week
            CurrentWeekStatus.IS_NEXT_WEEK -> R.string.next_week
            CurrentWeekStatus.NOT_CURRENT_WEEK -> R.string.not_current_week
        }

    fun getErrorMsgResId(errorReason: ErrorReason): Int? =
        when (errorReason) {
            ErrorReason.ALREADY_LOGIN -> R.string.already_login
            ErrorReason.INPUT_ERROR -> R.string.input_error
            ErrorReason.PASSWORD_ERROR -> R.string.password_error
            ErrorReason.SERVER_ERROR -> R.string.server_error
            ErrorReason.UNKNOWN -> R.string.unknown_error
            ErrorReason.CONNECTION_ERROR -> R.string.connection_error
            ErrorReason.NONE -> null
        }

    fun getLoadingProcessResId(loadingProcess: LoadingProcess): Int? =
        when (loadingProcess) {
            LoadingProcess.DATA_COMPAT -> R.string.data_compat
            LoadingProcess.LOGGING_SSO -> R.string.is_logging
            LoadingProcess.LOGGING_JWC -> R.string.is_logging_jwc
            LoadingProcess.CACHING -> R.string.is_caching
            LoadingProcess.NONE -> null
        }

    fun getNewsPostSourceResId(postSource: PostSource): Int? =
        when (postSource) {
            PostSource.ALSTU -> R.string.news_source_alstu
            PostSource.JWC -> R.string.news_source_jwc
            PostSource.RSS_JW -> R.string.news_source_rss_jw
            PostSource.RSS_TW -> R.string.news_source_rss_tw
            PostSource.RSS_XGC -> R.string.news_source_rss_xgc
            PostSource.RSS_XXB -> R.string.news_source_rss_xxb
            PostSource.UNKNOWN -> null
        }

    fun getContentErrorResId(contentErrorReason: ContentErrorReason): Int? =
        when (contentErrorReason) {
            ContentErrorReason.NONE -> null
            ContentErrorReason.TIMEOUT -> R.string.content_error_timeout
            ContentErrorReason.SERVER_ERROR -> R.string.server_error
            ContentErrorReason.OPERATION -> R.string.content_error_operation
            ContentErrorReason.PARSE_FAILED -> R.string.content_error_parse_failed
            ContentErrorReason.EMPTY_DATA -> R.string.content_error_empty_data
            ContentErrorReason.CONNECTION_ERROR -> R.string.connection_error
            ContentErrorReason.UNKNOWN -> R.string.unknown_error
        }

    fun getCourseTypeResId(courseType: CourseType): Int =
        when (courseType) {
            CourseType.COMPULSORY -> R.string.course_type_compulsory
            CourseType.MAJOR_SELECTIVE -> R.string.course_type_major_selective
            CourseType.OPTIONAL -> R.string.course_type_optional
            CourseType.PRACTICAL -> R.string.course_type_practical
        }

    fun getLearningProcessSubjectTypeResId(subjectType: SubjectType): Int =
        when (subjectType) {
            SubjectType.TARGET -> R.string.learning_process_target_credit
            SubjectType.REVISED -> R.string.learning_process_revised_credit
            SubjectType.BALANCE -> R.string.learning_process_balance_credit
            SubjectType.BONUS -> R.string.learning_process_bonus_credit
        }

    fun getTodayCourseNotifyTypeResId(arrangeNotifyType: CourseArrangeNotifyType): Int =
        when (arrangeNotifyType) {
            CourseArrangeNotifyType.COURSE_OR_TERM_DATA_EMPTY -> R.string.today_course_data_empty
            CourseArrangeNotifyType.LOADING_DATA -> R.string.today_course_loading
            CourseArrangeNotifyType.NO_TODAY_COURSE -> R.string.today_course_no_course
        }

    fun getImageOperationTypeResId(imageOperationType: ImageOperationType): Int =
        when (imageOperationType) {
            ImageOperationType.IMAGE_SAVE_SUCCESS -> R.string.image_save_success
            ImageOperationType.IMAGE_SAVE_FAILED -> R.string.image_save_failed
            ImageOperationType.IMAGE_SHARE_FAILED -> R.string.image_share_failed
        }
}