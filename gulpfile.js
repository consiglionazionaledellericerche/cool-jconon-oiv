var gulp = require('gulp');
var watch = require('gulp-watch');
var livereload = require('gulp-livereload');
var less = require('gulp-less');
var path = require('path');


gulp.task('less', function () {

  var source = '../less/style.less';

  return gulp.src(source)
    .pipe(watch(source, {base: source}))
    .pipe(less({
      paths: [ path.join(__dirname, 'less', 'includes') ]
    }))
    .pipe(gulp.dest('../../../target/classes/META-INF/css/css/'))
    .pipe(livereload());


});


gulp.task('watch-js', ['livereload'], function() {

  var source = './META-INF/js';

  gulp.src(source + '/**/*', {base: source})
    .pipe(watch(source, {base: source}))
    .pipe(gulp.dest('../../../target/classes/META-INF/js'))
    .pipe(livereload());
});

gulp.task('watch-pages', ['livereload'], function() {

  var source = './pages';

  gulp.src(source + '/**/*', {base: source})
    .pipe(watch(source, {base: source}))
    .pipe(gulp.dest('../../../target/classes/pages'))
    .pipe(livereload());
});


gulp.task('livereload', function() {
  livereload.listen();
});

gulp.task('default', ['watch-js', 'watch-pages', 'less']);

