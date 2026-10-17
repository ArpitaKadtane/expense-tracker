export default function CategoryFilter({ categories, value, onChange, disabled }) {
  return (
    <div className="category-filter">
      <label className="field-label" htmlFor="category-filter">
        Filter by category
      </label>
      <div className="select-wrap">
        <select
          id="category-filter"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          disabled={disabled}
        >
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
        <svg
          className="select-chevron"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="m6 9 6 6 6-6" />
        </svg>
      </div>
    </div>
  )
}
