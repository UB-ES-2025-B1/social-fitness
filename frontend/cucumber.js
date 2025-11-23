export default {
  default: {
    require: ['features/step_definitions/**/*.js', 'features/support/**/*.js'],
    format: ['progress', '@cucumber/pretty-formatter'],
    publishQuiet: true,
    parallel: 1,
  },
  html: {
    require: ['features/step_definitions/**/*.js', 'features/support/**/*.js'],
    format: ['html:cucumber-report.html'],
    publishQuiet: true,
  },
};
