import com.github.joostvdg.buming.sorting.MergeSort;

module joostvdg.buming.sorting {
    requires joostvdg.buming.api;
    requires joostvdg.buming.logging;
    uses com.github.joostvdg.buming.logging.Logger;

    provides com.github.joostvdg.buming.api.SortingExample with
        com.github.joostvdg.buming.sorting.BinarySearch,
        MergeSort,
        com.github.joostvdg.buming.sorting.SelectionSort;
}
