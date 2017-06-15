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


/*** RSVP Form ***/

const RsvpStage = {
  CODE: 'code',
  INVITE:  'invite',
  MENU:  'menu'
};

var rvspForm = new Vue({
  el: "#rsvpFroms",
  data: {
    rsvpCode: '',
    invitation: {},
    invitationJson: '',
    stage: RsvpStage.CODE,
    isFetching: false
  },
  methods: {
    submitCode: function() {
      var vm = this;
      this.isFetching = true;
      console.log(this.rsvpCode);
      axios.get('/api/invitations/'+this.rsvpCode)
        .then(function(rsp) {
          console.log(rsp);
          vm.isFetching = false;
          vm.stage = RsvpStage.INVITE;
          vm.invitation = rsp.data;
          vm.invitationJson = JSON.stringify(rsp.data);
        })
        .catch(function(err) {
          console.error(err);
        });
    },
    submitRsvp: function() {

    },
    updateInvitation: function(inv) {
      console.log('Update Invitation');
      this.invitation = inv;
    }
  }
});

Vue.component('guest', {
  // declare the props
  props: ['invitation', 'guest', 'index'],
  data: function() {
    return {
      isAddingGuest: false,
      applied: false,
      guestOf: null
    }
  },
  methods: {
    toggleAppliedCover: function() {
      var vm = this;
      vm.applied = true;
      setTimeout(function() {
        vm.applied = false;
      }, 1000);
    },
    rsvp: function() {
      console.log('rsvp');
      var vm = this;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/rsvp', {rsvp: true})
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
    },
    unrsvp: function() {
      console.log('unrsvp');
      var vm = this;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/rsvp', {rsvp: false})
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
    },
    addPlusOne: function() {
      console.log('addPlusOne');
      this.isAddingGuest = true;
    },
    removePlusOne: function() {
      console.log('removePlusOne');
      var vm = this;
      this.isAddingGuest = false;
      var invitedGuest = this.getInvitedGuest();
      if (invitedGuest !== null) {
        axios.delete('/api/invitations/'+this.invitation.code+'/guests/'+invitedGuest.key)
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
      }
    },
    hasAddedGuest: function() {
      var vm = this;
      return vm.guest.plusOne && vm.invitation.guests.reduce(function(acc, g) {
        return vm.isAddingGuest || acc || (g.plusOneGuestKey === vm.guest.key);
      }, false);
    },
    guestAdded: function(data) {
      console.log('guest added: ');
      console.log(data.newGuest);
      this.toggleAppliedCover();
      this.$emit('update-invitation', data.invitation);
    },
    getInvitedGuest: function() {
      var vm = this;
      var invitedGuests = vm.invitation.guests.filter(function(g) {
        return g.plusOneGuestKey === vm.guest.key;
      });
      if (invitedGuests.length > 0) {
        return invitedGuests[0];
      }
      return null;
    },
    getGuestOf: function() {
      var vm = this;
      return vm.invitation.guests.filter(function(g) {
        return g.key === vm.guest.plusOneGuestKey;
      })[0];
    }
  },
  // just like data, the prop can be used inside templates
  // and is also made available in the vm as this.message
  template: `
  <div class="invitation-guest">
      <transition name="fade">
        <div class="updated-cover" v-if="applied"><i class="title fa fa-check-circle"></i></div>
      </transition>
      <h5 class="guest-name">{{guest.firstname + ' ' + guest.lastname}}<span v-if="guest.plusOneGuestKey !== undefined">(Guest of {{ getGuestOf().firstname }})</span></h5>
      <div class="question will-attend" v-if="guest.plusOneGuestKey === undefined">
          <p>Will {{guest.firstname}} be attending?</p>
          <a class="answer" v-on:click="rsvp" v-bind:class="{ selected: guest.rsvp }">Yes</a>
          <a class="answer" v-on:click="unrsvp" v-bind:class="{ selected: !guest.rsvp }">No</a>
      </div>

      <hr v-if="guest.plusOne"/>
      <div class="question plus-one" v-if="guest.plusOne">
          <p>Will {{guest.firstname}} be bringing a guest?</p>
          <a class="answer" v-on:click="addPlusOne" v-bind:class="{ selected: hasAddedGuest() }">Yes</a>
          <a class="answer" v-on:click="removePlusOne" v-bind:class="{ selected: !hasAddedGuest() }">No</a>
          <add-guest  v-if="(isAddingGuest && !guest.hasAddedPlusOne)"
                      v-on:guest-added="guestAdded"
                      v-bind:invitationCode="invitation.code" 
                      v-bind:addedByGuest="guest" />
      </div>
      
      <!-- <p v-if="index < invitation.guests.length - 1">AND</p> -->
  </div>
  `
});

Vue.component('add-guest', {
  // declare the props
  props: ['invitationCode', 'addedByGuest'],
  data: function() {
    return {
      firstname: '',
      lastname: '',
    }
  },
  methods: {
    addGuest: function() {
      var vm = this;
      var guest = {
        firstname: this.firstname,
        lastname: this.lastname,
        guestOf: this.addedByGuest,
        invitationCode: this.invitationCode
      };
      axios.post('/api/invitations/'+this.invitationCode+'/guests/'+this.addedByGuest.key+'/add', guest)
        .then(function(rsp) {
          console.log(rsp);
          vm.$emit('guest-added', {
            newGuest: guest,
            invitation: rsp.data
          })
        })
        .catch(function(err) {
          console.error(err);
        });
    }
  },
  template: `
  <div class="row">
    <div class="col-sm-10 col-sm-offset-1">
      <div class="row add-guest">
        <form class="add-guest-form" v-on:submit.prevent="addGuest">
          <input type="text" v-model="firstname" class="form-control col-md-8 col-md-offset-1" name="firstname" placeholder="First Name" required data-validation-required-message="Please enter the guests first name" />
          <input type="text" v-model="lastname" class="form-control col-md-8 col-md-offset-1" name="lastname" placeholder="Last Name" required data-validation-required-message="Please enter the guests first name" />
          <input class="btn btn-primary mt5 add-guest-submit" type="submit" name="addGuest" value="Add Guest" />
        </form>
      </div>
    </div>
  </div>
  `
});
/*** RSVP Form End ***/

