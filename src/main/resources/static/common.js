function runCouponsPromise(couponsPromise){
    couponsPromise.then((successMessage) => {
        $('#coupons').DataTable({
            data: JSON.parse(successMessage),
            "columns": [
                {
                    "data": "dateReceived",
                    "width": "100px"
                },
                {
                    "data": "account",
                    "render": function(data, type, row) {
                        return row.account.username;
                    },
                    "width": "100px"
                },
                {
                    "data": "subject",
                    "render": function ( data, type, row, meta ) {
                        return '<a ' + (row.readInd ? '' : 'style="font-weight:bold" ')
                        + 'href=' + getApiBaseUrl("/body") + '?uid=' + row.uid + '>' + row.subject + '</a>';
                    }
                }
            ],
            "order": [[ 0, "desc" ]],
            responsive: true,
            "pageLength": 50
        });
    });
}