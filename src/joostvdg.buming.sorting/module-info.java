import com.github.joostvdg.buming.sorting.MergeSort;
import com.github.joostvdg.buming.sorting.SumProblem;
import com.github.joostvdg.buming.sorting.SelectionSort;
import com.github.joostvdg.buming.sorting.BinarySearch;

module joostvdg.buming.sorting {
    requires joostvdg.buming.api;
    requires joostvdg.buming.logging;
    uses com.github.joostvdg.buming.logging.Logger;

    provides com.github.joostvdg.buming.api.SortingExample with
        BinarySearch,
        MergeSort,
        SelectionSort,
        SumProblem;
}
