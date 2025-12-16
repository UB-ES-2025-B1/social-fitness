export default function () {
  return {
    default: {
      require: ['features/step_definitions/**/*.js', 'features/support/**/*.js'],
      format: ['progress', '@cucumber/pretty-formatter'],
      publishQuiet: true,
      parallel: 1,
    },
    html: {
      require: ['features/step_definitions/**/*.js', 'features/support/**/*.js'],
      format: ['progress', 'html:cucumber-report.html'],
      publishQuiet: true,
    },
  };
}
