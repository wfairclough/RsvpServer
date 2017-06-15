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
    isFetching: false,
    menu: {
      items: []
    }
  },
  created: function() {
    var vm = this;
    axios.get('/api/menu/items')
      .then(function(rsp) {
        console.log(rsp);
        vm.menu.items = rsp.data;
      })
      .catch(function(err) {
        console.error(err);
      });
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
      // Calculate if any of the guests in the new invitation were just added
      var oldGuestKeys = this.invitation.guests.map(function(g) { return g.key });
      var updatedGuests = inv.guests.map(function(g) {
        g.justAdded = oldGuestKeys.indexOf(g.key) === -1;
        return g;
      });
      inv.guests = updatedGuests;
      this.invitation = inv;
    }
  }
});

Vue.component('guest', {
  // declare the props
  props: ['invitation', 'guest', 'index', 'menu'],
  data: function() {
    return {
      isAddingGuest: false,
      applied: false,
      guestOf: null,
      selectedMenuItem: null,
      plusOne: null
    }
  },
  created: function() {
    if (this.guest.justAdded) {
      this.toggleAppliedCover();
    }
    this.plusOne = this.getAddedGuest();
  },
  updated: function() {
    this.plusOne = this.getAddedGuest();
  },
  watch: {
    selectedMenuItem: function(newMenuItem) {
      console.log('Selected new menu Item: ' + newMenuItem);
      var vm = this;
      axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/menu', 
        {
          menuItemKey: newMenuItem,
          notes: ''
        })
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
          vm.isAddingGuest = false;
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
      var invitedGuest = this.getInvitedGuest();
      if (invitedGuest !== null) {
        axios.delete('/api/invitations/'+this.invitation.code+'/guests/'+invitedGuest.key)
        .then(function(rsp) {
          console.log(rsp);
          vm.plusOne = null;
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
          vm.isAddingGuest = false;
        })
        .catch(function(err) {
          console.error(err);
        });
      } else {
        // Lets let the server know that this guest has selected 'No' to bringing a guest
        axios.put('/api/invitations/'+this.invitation.code+'/guests/'+this.guest.key+'/noplusone', {hasPlusOne: false})
        .then(function(rsp) {
          console.log(rsp);
          vm.plusOne = null;
          vm.$emit('update-invitation', rsp.data);
          vm.toggleAppliedCover();
        })
        .catch(function(err) {
          console.error(err);
        });
      }
    },
    getAddedGuest: function() {
      var vm = this;
      return vm.guest.plusOne && vm.invitation.guests.reduce(function(acc, g) {
        if (g.plusOneGuestKey === vm.guest.key) {
          acc = g;
        }
        return acc;
      }, null);
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
    },
    isSelectedMenuItem: function(item) {
      var selectedKey = this.guest.menuItem && this.guest.menuItem.menuItemKey;
      return selectedKey === item.key
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
          <div class="answers" v-bind:class="{ invalid: (guest.rsvp === undefined) }">
            <a class="answer" v-on:click="rsvp" v-bind:class="{ selected: guest.rsvp === true }">Yes</a>
            <a class="answer" v-on:click="unrsvp" v-bind:class="{ selected: guest.rsvp === false }">No</a>
          </div>
      </div>
      
      <hr v-if="guest.rsvp && guest.plusOneGuestKey === undefined"/>
      <div class="question select-meal" v-if="guest.rsvp">
          <p>Please select {{guest.firstname}}'s choice entr&eacute;e?</p>
          <div class="menu" v-bind:class="{ invalid: (guest.menuItem === undefined) }">
            <div class="menu-item" 
              v-for="item in menu.items" 
              v-bind:class="{ selected: isSelectedMenuItem(item) }"
              @click="selectedMenuItem = item.key">
                <a class="answer">{{ item.name }}</a>
                <p>{{ item.description }}</p>
            </div>
          </div>
      </div>
      
      <hr v-if="guest.plusOne && guest.rsvp"/>
      <div class="question plus-one" v-if="guest.plusOne && guest.rsvp">
          <p>Will {{guest.firstname}} be bringing a guest?</p>
          <div class="answers" v-bind:class="{ invalid: (guest.hasAddedPlusOne === undefined) }">
            <a class="answer" v-on:click="addPlusOne" v-bind:class="{ selected: (guest.hasAddedPlusOne === true || (guest.hasAddedPlusOne === undefined && isAddingGuest)) }">Yes</a>
            <a class="answer" v-on:click="removePlusOne" v-bind:class="{ selected: guest.hasAddedPlusOne === false }">No</a>
            <p v-if="plusOne !== null">({{ plusOne.firstname + ' ' + plusOne.lastname }})</p>
            <add-guest  v-if="(isAddingGuest && !guest.hasAddedPlusOne)"
                        v-on:guest-added="guestAdded"
                        v-bind:invitationCode="invitation.code" 
                        v-bind:addedByGuest="guest" />
          </div>
      </div>

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

