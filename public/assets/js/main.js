var app5 = new Vue({
  el: '#welcome',
  data: {
    message: 'Katrina & Will'
  },
  methods: {
    reverseMessage: function () {
      this.message = this.message.split('').reverse().join('')
    }
  }
})
